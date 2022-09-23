package com.lucasprojects.dscatalog.tests;

import java.time.Instant;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.entities.Product;
import com.lucasprojects.dscatalog.entities.dtos.CategoryDTO;
import com.lucasprojects.dscatalog.entities.dtos.ProductDTO;

public class Factory {

	public static Product createProduct() {
		Product product = new Product(1L, "Phone", "Good phone", 800.0, "https://img.com/img.png", Instant.now());
		product.getCategories().add(new Category(2L, "Eletronics"));
		return product;
	}
	
	public static Product createProductWithoutId() {
		Product product = new Product(null, "Phone", "Good phone", 800.0, "https://img.com/img.png", Instant.now());
		product.getCategories().add(new Category(2L, "Eletronics"));
		return product;
	}
	
	public static ProductDTO createProductDTO() {
		Product product = createProduct();
		return new ProductDTO(product, product.getCategories());
	}
	
	public static Category createCategory() {
		Category category = new Category(1L, "Fashion");
		return category;
	}
	
	public static Category createCategoryWithoutId() {
		Category category = new Category(null, "Fashion");
		return category;
	}
	
	public static CategoryDTO createCategoryDTO() {
		Category category = createCategory();
		return new CategoryDTO(category);
	}
}
