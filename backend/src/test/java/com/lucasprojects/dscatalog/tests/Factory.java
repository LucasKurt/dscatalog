package com.lucasprojects.dscatalog.tests;

import java.time.Instant;

import com.lucasprojects.dscatalog.entities.Category;
import com.lucasprojects.dscatalog.entities.Product;
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
}
