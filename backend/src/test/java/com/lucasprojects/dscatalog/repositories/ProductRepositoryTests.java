package com.lucasprojects.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.lucasprojects.dscatalog.entities.Product;
import com.lucasprojects.dscatalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTests {

	@Autowired
	private ProductRepository repository;
	
	private long exsistsId;
	private long nonExsistsId;
	private long countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception {
		exsistsId = 1L;
		nonExsistsId = 1000L;
		countTotalProducts = 25L;
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		Product product = Factory.createProductWithoutId();
		
		product = repository.save(product);
		
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
	}
	
	@Test
	public void saveShouldUpdateProductWhenIdIsNotNull() {
		Product product = repository.getReferenceById(1L);		
		String productName = product.getName();
		
		product = repository.save(Factory.createProduct());
		String updatedName = product.getName();
		
		Assertions.assertNotEquals(productName, updatedName);
	}

	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		repository.deleteById(exsistsId);

		Optional<Product> result = repository.findById(exsistsId);
		Assertions.assertFalse(result.isPresent());
	}

	@Test
	public void deleteShouldThrowExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExsistsId);
		});
	}
	
	@Test
	public void findByIdShouldReturnOptionalWhenIdExists() {
		Optional<Product> product = repository.findById(exsistsId);
		
		Assertions.assertTrue(product.isPresent());
	}
	
	@Test
	public void findByIdShouldReturnEmptyOptionalWhenIdNotExists() {
		Optional<Product> result = repository.findById(nonExsistsId);
		
		Assertions.assertFalse(result.isPresent());
	}
}
