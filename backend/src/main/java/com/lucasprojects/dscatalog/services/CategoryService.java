package com.lucasprojects.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.entities.dtos.CategoryDTO;
import com.lucasprojects.dscatalog.repositories.CategoryRepository;
import com.lucasprojects.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository repository;

	@Transactional(readOnly = true)
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		
		return list.stream().map(category -> new CategoryDTO(category)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> obj = repository.findById(id);
		Category category = obj.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		
		return new CategoryDTO(category);
	}

	@Transactional(readOnly = true)
	public CategoryDTO insert(CategoryDTO dto) {
		Category category = new Category(null, dto.getName());		
		category = repository.save(category);
		
		return new CategoryDTO(category);
	}
}
