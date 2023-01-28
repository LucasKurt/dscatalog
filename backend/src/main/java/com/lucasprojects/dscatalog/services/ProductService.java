package com.lucasprojects.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.entities.Product;
import com.lucasprojects.dscatalog.entities.dtos.ProductDTO;
import com.lucasprojects.dscatalog.repositories.CategoryRepository;
import com.lucasprojects.dscatalog.repositories.ProductRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAll(Long categoryId, String name, Pageable pageable) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(categoryRepository.getReferenceById(categoryId));
		Page<Product> page = repository.find(categories, name, pageable);

		return page.map(entity -> new ProductDTO(entity));
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new EntityNotFoundException("Unable to find product with id " + id));

		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		dtoToProduct(dto, entity);
		entity = repository.save(entity);

		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getReferenceById(id);
			dtoToProduct(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		} catch (EntityNotFoundException e) {
			if (e.getMessage().contains("category")) {
				throw new EntityNotFoundException(e.getMessage());
			}
			throw new EntityNotFoundException("Unable to find product with id " + id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new EntityNotFoundException("Unable to find product with id " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityViolationException("Integrity violation");
		}
	}

	private void dtoToProduct(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setImgUrl(dto.getImgUrl());
		entity.setDate(dto.getDate());
		entity.getCategories().clear();

		dto.getCategories().forEach(catDto -> {
			try {
				Category category = categoryRepository.getReferenceById(catDto.getId());
				entity.getCategories().add(category);
			} catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Unable to find category with id " + catDto.getId());
			}
		});
	}
}
