package com.product.product_service;

import com.product.product_service.dto.ProductDTO;
import com.product.product_service.repository.ProductRepository;
import com.product.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceImplTest {

	@Autowired
	ProductService productService;
	@Autowired ProductRepository productRepository;

	@Test
	void createKeepsStock() {
		ProductDTO dto = new ProductDTO();
		dto.setName("Mug");
		dto.setPrice(10.5);
		dto.setStock(5);

		ProductDTO saved = productService.addProduct(dto, null);

		assertEquals(5, saved.getStock());
		assertEquals("Mug", saved.getName());
	}

	@Test
	void partialUpdateKeepsOtherFields() {
		ProductDTO dto = new ProductDTO();
		dto.setName("Mug");
		dto.setPrice(10.5);
		dto.setStock(5);
		ProductDTO saved = productService.addProduct(dto, null);
		long rowsBefore = productRepository.count();

		ProductDTO patch = new ProductDTO();
		patch.setPrice(12.0);
		ProductDTO updated = productService.updateProduct(saved.getId(), patch, null);

		assertEquals(saved.getId(), updated.getId());
		assertEquals(12.0, updated.getPrice());
		assertEquals("Mug", updated.getName());
		assertEquals(5, updated.getStock());
		assertEquals(rowsBefore, productRepository.count());   // no new row
	}

	@Test
	void explicitZeroStockIsApplied() {
		ProductDTO dto = new ProductDTO();
		dto.setName("Mug");
		dto.setPrice(10.5);
		dto.setStock(5);
		ProductDTO saved = productService.addProduct(dto, null);

		ProductDTO patch = new ProductDTO();
		patch.setStock(0);
		ProductDTO updated = productService.updateProduct(saved.getId(), patch, null);

		assertEquals(0, updated.getStock());
		assertEquals(10.5, updated.getPrice());
	}
}