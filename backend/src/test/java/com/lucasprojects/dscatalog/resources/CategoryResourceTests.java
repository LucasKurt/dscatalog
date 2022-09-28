package com.lucasprojects.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucasprojects.dscatalog.entities.dtos.CategoryDTO;
import com.lucasprojects.dscatalog.services.CategoryService;
import com.lucasprojects.dscatalog.tests.Factory;

@WebMvcTest(CategoryResource.class)
public class CategoryResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CategoryService service;

	@Autowired
	private ObjectMapper mapper;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;

	private CategoryDTO dto;
	private List<CategoryDTO> list;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;

		dto = Factory.createCategoryDTO();
		list = Arrays.asList(dto);

		when(service.findAll()).thenReturn(list);

		when(service.findById(existingId)).thenReturn(dto);
		when(service.findById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		when(service.insert(any())).thenReturn(dto);

		when(service.update(eq(existingId), any())).thenReturn(dto);
		when(service.update(eq(nonExistingId), any())).thenThrow(EntityNotFoundException.class);
		
		doNothing().when(service).delete(existingId);
		doThrow(EntityNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DataIntegrityViolationException.class).when(service).delete(dependentId);

	}

	@Test
	public void findAllShouldReturnPage() throws Exception {
		ResultActions result = mockMvc.perform(get("/categories").accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
	}

	@Test
	public void findByIdShouldReturnCategoryWhenIdExists() throws Exception {
		ResultActions result = mockMvc.perform(get("/categories/{id}", existingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdNotExists() throws Exception {
		ResultActions result = mockMvc.perform(get("/categories/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void insertShouldReturnCategoryDTO() throws Exception {
		String jsonBody = mapper.writeValueAsString(dto);
		ResultActions result = mockMvc.perform(post("/categories").content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
	}

	@Test
	public void updateShouldReturnCategoryDTOWhenIdExists() throws Exception {
		String jsonBody = mapper.writeValueAsString(dto);
		ResultActions result = mockMvc.perform(put("/categories/{id}", existingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
	}

	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		String jsonBody = mapper.writeValueAsString(dto);
		ResultActions result = mockMvc.perform(put("/categories/{id}", nonExistingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
		ResultActions result = mockMvc.perform(delete("/categories/{id}", existingId));

		result.andExpect(status().isNoContent());
	}

	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		ResultActions result = mockMvc
				.perform(delete("/categories/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void deleteShouldReturnBadRequestWhenIdIsDependent() throws Exception {
		ResultActions result = mockMvc
				.perform(delete("/categories/{id}", dependentId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isBadRequest());
	}
}
