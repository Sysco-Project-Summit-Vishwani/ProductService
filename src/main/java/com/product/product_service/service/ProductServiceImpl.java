package com.product.product_service.service;

import org.springframework.stereotype.Service;
import com.product.product_service.dto.ProductDTO;
import com.product.product_service.model.Product;
import com.product.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.product.product_service.exception.ProductNotFoundException;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final Logger logger = LogManager.getLogger(ProductServiceImpl.class);
    private final String uploadDir = "uploads/";
    @Override
    public List<ProductDTO> getAllProducts() {
            logger.info("Fetching all products");
            List<Product> products = productRepository.findAll();
            return products.stream()
                    .map(product -> modelMapper.map(product, ProductDTO.class))
                    .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {
            logger.info("Fetching product with ID: {}", id);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id));
            return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, MultipartFile imageFile) {
            logger.info("Adding a new product");
            Product product = modelMapper.map(productDTO, Product.class);
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveImageFile(imageFile); // Save the image and get its URL
                product.setImageUrl(imageUrl); // Set image URL in product
            }
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile imageFile) {
            logger.info("Updating product with ID: {}", id);
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id));
            modelMapper.map(productDTO, existingProduct);
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveImageFile(imageFile); // Save the new image and get the URL
                existingProduct.setImageUrl(imageUrl); // Update the product's image URL
            }

            Product updatedProduct = productRepository.save(existingProduct);
            return modelMapper.map(updatedProduct, ProductDTO.class);
    }
    private String saveImageFile(MultipartFile file) {
        try {
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
            String ext = original.contains(".")
                    ? original.substring(original.lastIndexOf('.')).toLowerCase()
                    : "";
            if (!List.of(".jpg", ".jpeg", ".png", ".webp").contains(ext)) {
                throw new IllegalArgumentException("Unsupported image type");
            }

            String fileName = UUID.randomUUID() + ext;   // no user-controlled name
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);          // no-op if it exists

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            return uploadDir + fileName;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save image file", e);
        }
    }
    @Override
    public void deleteProduct(Long id) {
            logger.info("Deleting product with ID: {}", id);
            if (!productRepository.existsById(id)) {
                throw new ProductNotFoundException(id);
            }
            productRepository.deleteById(id);
    }
}
