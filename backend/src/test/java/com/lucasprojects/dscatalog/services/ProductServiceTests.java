package com.lucasprojects.dscatalog.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.entities.Product;
import com.lucasprojects.dscatalog.entities.dtos.ProductDTO;
import com.lucasprojects.dscatalog.repositories.CategoryRepository;
import com.lucasprojects.dscatalog.repositories.ProductRepository;
import com.lucasprojects.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository repository;

	@Mock
	private CategoryRepository categoryRepository;

	private long existingId;
	private long nonExistingId;
	private long dependentId;

	private Product entity;
	private PageImpl<Product> page;
	private Category category;

	private ProductDTO dto;
	private Product entityWithInvalidCategory;
	private ProductDTO dtoWithInvalidCategory;

	@BeforeEach
	void setup() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;

		entity = Factory.createProduct(existingId, existingId);
		page = new PageImpl<>(List.of(entity));
		category = Factory.createCategory(existingId);

		dto = Factory.createProductDTO(entity);
		entityWithInvalidCategory = Factory.createProduct(existingId, nonExistingId);
		dtoWithInvalidCategory = Factory.createProductDTO(entityWithInvalidCategory);

		Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);
		Mockito.when(repository.find(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(page);

		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(entity));
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		Mockito.when(repository.getReferenceById(existingId)).thenReturn(entity);
		Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(categoryRepository.getReferenceById(existingId)).thenReturn(category);
		Mockito.when(categoryRepository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		Mockito.when(repository.save((Product) ArgumentMatchers.any())).thenReturn(entity);

		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}

	@Test
	public void findAllPagedShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<ProductDTO> result = service.findAll(0L, "", pageable);

		Assertions.assertNotNull(result);
		Mockito.verify(repository, Mockito.times(1)).find(null, "", pageable);
	}

	@Test
	public void findByIdShouldReturnProductWhenIdExists() {
		ProductDTO dto = service.findById(existingId);

		Assertions.assertNotNull(dto);
		Mockito.verify(repository, Mockito.times(1)).findById(existingId);
	}

	@Test
	public void findByIdShouldThrowEntityNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});

		Mockito.verify(repository, Mockito.times(1)).findById(nonExistingId);
	}

	@Test
	public void insertShouldReturnProductWhenCategoryExists() {
		ProductDTO result = service.insert(dto);

		Assertions.assertNotNull(result);

		Mockito.verify(categoryRepository, Mockito.times(1)).getReferenceById(existingId);
		Mockito.verify(repository, Mockito.times(1)).save(Factory.createProduct(null, existingId));
	}

	@Test
	public void insertShouldThrowEntityNotFoundExceptionWhenWhenCategoryDoesNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.insert(dtoWithInvalidCategory);
		});

		Mockito.verify(categoryRepository, Mockito.times(1)).getReferenceById(nonExistingId);
	}

	@Test
	public void updateShouldReturnProductWhenIdExists() {
		ProductDTO result = service.update(existingId, dto);

		Assertions.assertNotNull(result);

		Mockito.verify(repository, Mockito.times(1)).getReferenceById(existingId);
		Mockito.verify(categoryRepository, Mockito.times(1)).getReferenceById(existingId);
		Mockito.verify(repository, Mockito.times(1)).save(entity);
	}

	@Test
	public void updateShouldThrowEntityNotFoundExceptionWhenWhenCategoryDoesNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.update(existingId, dtoWithInvalidCategory);
		});

		Mockito.verify(repository, Mockito.times(1)).getReferenceById(existingId);
		Mockito.verify(categoryRepository, Mockito.times(1)).getReferenceById(nonExistingId);
	}

	@Test
	public void updateShouldThrowEntityNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.update(nonExistingId, dto);
		});

		Mockito.verify(repository, Mockito.times(1)).getReferenceById(nonExistingId);
	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});

		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
	}

	@Test
	public void deleteShouldThrowEntityNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(EntityNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});

		Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);
	}

	@Test
	public void deleteShouldThrowDataIntegrityViolationExceptionWhenDependentId() {
		Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
			service.delete(dependentId);
		});

		Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
	}
}
