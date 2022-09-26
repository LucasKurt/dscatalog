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

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucasprojects.dscatalog.entities.Product;
import com.lucasprojects.dscatalog.entities.dtos.ProductDTO;
import com.lucasprojects.dscatalog.services.ProductService;
import com.lucasprojects.dscatalog.tests.Factory;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService service;

	@Autowired
	private ObjectMapper mapper;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;

	private ProductDTO dto;
	private PageImpl<ProductDTO> page;

	private Product entityWithInvalidCategory;
	private ProductDTO dtoWithInvalidCategory;

	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;

		dto = Factory.createProductDTO();
		page = new PageImpl<>(List.of(dto));

		entityWithInvalidCategory = Factory.createProduct(existingId, nonExistingId);
		dtoWithInvalidCategory = Factory.createProductDTO(entityWithInvalidCategory);

		when(service.findAllPaged(any())).thenReturn(page);

		when(service.findById(existingId)).thenReturn(dto);
		when(service.findById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		when(service.insert(any())).thenReturn(dto);

		when(service.update(eq(existingId), any())).thenReturn(dto);
		when(service.update(eq(nonExistingId), any())).thenThrow(EntityNotFoundException.class);
//		when(service.update(existingId, dtoWithInvalidCategory)).thenThrow(EntityNotFoundException.class);
		doThrow(EntityNotFoundException.class).when(service).update(existingId, dtoWithInvalidCategory);

		doNothing().when(service).delete(existingId);
		doThrow(EntityNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DataIntegrityViolationException.class).when(service).delete(dependentId);

	}

	@Test
	public void findAllShouldReturnPage() throws Exception {
		ResultActions result = mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
	}

	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		ResultActions result = mockMvc.perform(get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		result.andExpect(jsonPath("$.price").exists());
		result.andExpect(jsonPath("$.imgUrl").exists());
		result.andExpect(jsonPath("$.date").exists());
		result.andExpect(jsonPath("$.categories").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdNotExists() throws Exception {
		ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void insertShouldReturnProductDTO() throws Exception {
		String jsonBody = mapper.writeValueAsString(dto);
		ResultActions result = mockMvc.perform(post("/products").content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		result.andExpect(jsonPath("$.price").exists());
		result.andExpect(jsonPath("$.imgUrl").exists());
		result.andExpect(jsonPath("$.date").exists());
		result.andExpect(jsonPath("$.categories").exists());
	}

	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		String jsonBody = mapper.writeValueAsString(dto);
		ResultActions result = mockMvc.perform(put("/products/{id}", existingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
		result.andExpect(jsonPath("$.price").exists());
		result.andExpect(jsonPath("$.imgUrl").exists());
		result.andExpect(jsonPath("$.date").exists());
		result.andExpect(jsonPath("$.categories").exists());
	}

//	@Test
//	public void updateShouldReturnNotFoundWhenCategoryIdDoesNotExists() throws Exception {
//		String jsonBody = mapper.writeValueAsString(dtoWithInvalidCategory);
//		ResultActions result = mockMvc.perform(put("/products/{id}", existingId).content(jsonBody)
//				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
//
//		result.andExpect(status().isNotFound());
//	}

	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		String jsonBody = mapper.writeValueAsString(dto);
		ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId).content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void deleteShouldReturnNoContentWhenIdExistis() throws Exception {
		ResultActions result = mockMvc.perform(delete("/products/{id}", existingId));

		result.andExpect(status().isNoContent());
	}

	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExistis() throws Exception {
		ResultActions result = mockMvc
				.perform(delete("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isNotFound());
	}

	@Test
	public void deleteShouldReturnBadRequestWhenIdIsDependent() throws Exception {
		ResultActions result = mockMvc
				.perform(delete("/products/{id}", dependentId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isBadRequest());
	}
}
