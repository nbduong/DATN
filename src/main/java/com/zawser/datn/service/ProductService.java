package com.zawser.datn.service;

import com.zawser.datn.dto.request.ProductRequest;
import com.zawser.datn.dto.response.ProductResponse;
import com.zawser.datn.entity.Category;
import com.zawser.datn.entity.Product;
import com.zawser.datn.mapper.ProductMapper;
import com.zawser.datn.repository.CategoryRepository;
import com.zawser.datn.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {


    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse createProduct(ProductRequest request) throws IOException {
        // Convert DTO to Entity using ProductMapper
        Product product = productMapper.toProduct(request);

        // Handle categories
        if (request.getCategories() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategories());
            product.setCategories(categories);
        }

        String uploadDir = "uploads/";
        // Handle images
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        List<String> imagePaths = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            log.info("Saving product images:");
            for (MultipartFile image : request.getImages()) {
                if (!image.isEmpty()) {
                    try {
                        // Tạo tên file duy nhất để tránh trùng lặp
                        String fileName = image.getOriginalFilename();
                        Path filePath = Paths.get(uploadDir + fileName);
                        Files.copy(image.getInputStream(), filePath);
                        imagePaths.add("uploads/" + fileName);
                        log.info("Saved image: {}", filePath.toString());
                    } catch (IOException e) {
                        throw new IOException("Failed to save image: " + image.getOriginalFilename(), e);
                    }
                }
            }
            product.setImages(imagePaths); // Cập nhật danh sách đường dẫn ảnh vào entity
        }

        // Save the product
        product = productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toProductResponse(product);
    }

    public void deleteProduct(Long id) throws IOException {
        // Find the product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Delete associated images from the filesystem
        List<String> imagePaths = product.getImages();
        if (imagePaths != null) {
            for (String imagePath : imagePaths) {
                try {
                    Files.deleteIfExists(Paths.get(imagePath));
                } catch (IOException e) {
                    throw new IOException("Failed to delete image: " + imagePath, e);
                }
            }
        }

        // Delete the product from the database
        productRepository.delete(product);
    }
}