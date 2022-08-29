package com.lucasprojects.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> page = repository.findAll(pageRequest);

		return page.map(product -> new ProductDTO(product));
	}

	@Transactional(readOnly = true)
	public List<ProductDTO> findAllPagedWithCategories() {
		List<Product> list = repository.findAll();

		return list.stream().map(product -> new ProductDTO(product)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product product = obj.orElseThrow(() -> new EntityNotFoundException("Unable to find product with id " + id));

		return new ProductDTO(product, product.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product product = new Product();
		dtoToProduct(dto, product);
		product = repository.save(product);

		return new ProductDTO(product);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product product = repository.getReferenceById(id);
			dtoToProduct(dto, product);
			product = repository.save(product);
			return new ProductDTO(product);
		} catch (EntityNotFoundException e) {
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

	private void dtoToProduct(ProductDTO dto, Product product) {
		product.setName(dto.getName());
		product.setDescription(dto.getDescription());
		product.setPrice(dto.getPrice());
		product.setImgUrl(dto.getImgUrl());
		product.setDate(dto.getDate());
		product.getCategories().clear();
		
		dto.getCategories().forEach(catDto -> {
			try {
				Category category = categoryRepository.getReferenceById(catDto.getId());
				product.getCategories().add(category);
			} catch (EntityNotFoundException e) {
				throw new EntityNotFoundException("Unable to find category with id " + catDto.getId());
			}
		});
	}
}
