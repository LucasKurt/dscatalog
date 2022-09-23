package com.lucasprojects.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.tests.Factory;

@DataJpaTest
public class CategoryRepositoryTests {

	@Autowired
	private CategoryRepository repository;
	
	private long exsistsId;
	private long nonExsistsId;
	private long countTotalCategorys;
	
	@BeforeEach
	void setUp() throws Exception {
		exsistsId = 1L;
		nonExsistsId = 1000L;
		countTotalCategorys = 3L;
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		Category Category = Factory.createCategoryWithoutId();
		
		Category = repository.save(Category);
		
		Assertions.assertNotNull(Category.getId());
		Assertions.assertEquals(countTotalCategorys + 1, Category.getId());
	}
	
	@Test
	public void saveShouldUpdateCategoryWhenIdIsNotNull() {
		Category Category = repository.getReferenceById(1L);		
		String CategoryName = Category.getName();
		
		Category = repository.save(Factory.createCategory());
		String updatedName = Category.getName();
		
		Assertions.assertNotEquals(CategoryName, updatedName);
	}

	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		repository.deleteById(exsistsId);

		Optional<Category> result = repository.findById(exsistsId);
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
		Optional<Category> Category = repository.findById(exsistsId);
		
		Assertions.assertTrue(Category.isPresent());
	}
	
	@Test
	public void findByIdShouldReturnEmptyOptionalWhenIdNotExists() {
		Optional<Category> result = repository.findById(nonExsistsId);
		
		Assertions.assertFalse(result.isPresent());
	}
}
