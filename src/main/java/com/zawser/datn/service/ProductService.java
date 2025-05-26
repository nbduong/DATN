package com.zawser.datn.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zawser.datn.dto.request.ProductRequest;
import com.zawser.datn.dto.response.ProductResponse;
import com.zawser.datn.entity.Category;
import com.zawser.datn.entity.Product;
import com.zawser.datn.mapper.ProductMapper;
import com.zawser.datn.repository.CategoryRepository;
import com.zawser.datn.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductRequest request) throws IOException {
        Product product = productMapper.toProduct(request);

        if (request.getCategories() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategories());
            product.setCategories(categories);
        }

        String uploadDir = "uploads/";
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
            product.setImages(imagePaths);
        }

        product = productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toProductResponse(product);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(Long id, ProductRequest request) throws IOException {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productMapper.updateProduct(product, request);

        if (request.getCategories() != null) {
            List<Category> categories = categoryRepository.findAllById(request.getCategories());
            product.setCategories(categories);
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            if (product.getImages() != null) {
                for (String imagePath : product.getImages()) {
                    Files.deleteIfExists(Paths.get(imagePath));
                }
            }

            String uploadDir = "uploads/";
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile image : request.getImages()) {
                if (!image.isEmpty()) {
                    String fileName = image.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.copy(image.getInputStream(), filePath);
                    imagePaths.add("uploads/" + fileName);
                }
            }
            product.setImages(imagePaths);
        }

        product = productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(Long id) throws IOException {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

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
        productRepository.delete(product);
    }
}
