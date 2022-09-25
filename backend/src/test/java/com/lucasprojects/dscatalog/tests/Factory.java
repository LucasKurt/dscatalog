package com.lucasprojects.dscatalog.tests;

import java.time.Instant;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.entities.Product;
import com.lucasprojects.dscatalog.entities.dtos.CategoryDTO;
import com.lucasprojects.dscatalog.entities.dtos.ProductDTO;

public class Factory {

	public static Product createProduct(Long id, Long categoryId) {
		Product product = new Product(id, "Phone", "Good phone", 800.0, "https://img.com/img.png", Instant.now());
		product.getCategories().add(createCategory(categoryId));
		return product;
	}
	
	public static ProductDTO createProductDTO(Product product) {
		return new ProductDTO(product, product.getCategories());
	}
	
	public static Category createCategory(Long id) {
		Category category = new Category(id, "Eletronics");
		return category;
	}
	
	public static CategoryDTO createCategoryDTO(Category category) {
		return new CategoryDTO(category);
	}
}
