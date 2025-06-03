package com.zawser.datn.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zawser.datn.dto.request.ProductRequest;
import com.zawser.datn.dto.response.ProductResponse;
import com.zawser.datn.entity.Brand;
import com.zawser.datn.entity.Category;
import com.zawser.datn.entity.Product;
import com.zawser.datn.entity.ProductSpecification;
import com.zawser.datn.mapper.ProductMapper;
import com.zawser.datn.repository.BrandRepository;
import com.zawser.datn.repository.CategoryRepository;
import com.zawser.datn.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductRequest request) throws IOException {

        Map<String, String> specMap;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            specMap = objectMapper.readValue(
                    request.getSpecificationsJson(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new IOException("Failed to parse specifications JSON", e);
        }

        Product product = productMapper.toProduct(request);

        product.setBrand(brandRepository
                .findById(request.getBrandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + request.getBrandId())));
        product.setCategory(categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId())));

        List<String> imagePaths = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new IOException("Failed to create upload directory", e);
            }

            log.info("Saving product images:");
            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                    String fileName = System.currentTimeMillis() + "_" + originalFileName;
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    imagePaths.add(uploadDir + fileName);
                    log.info("Saved image: {}", filePath);
                }
            }
            product.setImages(imagePaths);
        }

        List<ProductSpecification> specs = specMap.entrySet().stream()
                .map(e -> new ProductSpecification(e.getKey(), e.getValue(), product))
                .collect(Collectors.toList());
        product.setSpecifications(specs);

        product.setCreatedBy("ADMIN");
        product.setLastModifiedBy("ADMIN");
        product.setLastModifiedDate(LocalDate.now());
        product.setCreatedDate(LocalDate.now());
        product.setViewCount(0L);
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setViewCount(1 + product.getViewCount());
        productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(String id, ProductRequest request) throws IOException {

        log.info("Updating product with id: {}", id);
        // Validate input
        if (id == null) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (request == null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        log.info("Fetching product with id: {}", id);
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        log.info("Updating product fields for id: {}", id);
        productMapper.updateProduct(product, request);

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category with ID " + request.getCategoryId() + " not found"));
        product.setCategory(category);

        // Lấy thực thể Brand từ repository
        Brand brand = brandRepository
                .findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand with ID " + request.getBrandId() + " not found"));
        product.setBrand(brand);

        log.debug("Parsing specifications JSON");
        Map<String, String> specMap;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            specMap = objectMapper.readValue(
                    request.getSpecificationsJson(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            log.error("Failed to parse specifications JSON", e);
            throw new IOException("Failed to parse specifications JSON", e);
        }

        if (product.getSpecifications() == null) {
            product.setSpecifications(new ArrayList<>());
        }

        Map<String, ProductSpecification> existingSpecs = product.getSpecifications().stream()
                .collect(Collectors.toMap(ProductSpecification::getSpecKey, spec -> spec));

        List<ProductSpecification> updatedSpecs = new ArrayList<>();
        for (Map.Entry<String, String> entry : specMap.entrySet()) {
            ProductSpecification spec = existingSpecs.get(entry.getKey());
            if (spec != null) {
                spec.setSpecValue(entry.getValue());
                updatedSpecs.add(spec);
            } else {
                updatedSpecs.add(new ProductSpecification(entry.getKey(), entry.getValue(), product));
            }
        }

        product.getSpecifications().clear();
        product.getSpecifications().addAll(updatedSpecs);

        List<String> imagePaths =
                new ArrayList<>(product.getImages() != null ? product.getImages() : Collections.emptyList());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                log.error("Failed to create upload directory", e);
                throw new IOException("Failed to create upload directory", e);
            }

            log.info("Saving product images for update:");
            for (MultipartFile image : request.getImages()) {
                if (image != null && !image.isEmpty()) {
                    try {
                        String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                        String fileName = System.currentTimeMillis() + "_" + originalFileName;
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                        imagePaths.add(uploadDir + fileName);
                        log.info("Saved image: {}", filePath);
                    } catch (IOException e) {
                        log.error("Failed to save image: {}", image.getOriginalFilename(), e);
                        throw new IOException("Failed to save image: " + image.getOriginalFilename(), e);
                    }
                }
            }
            product.setImages(imagePaths);
        }

        product.setLastModifiedBy("ADMIN");
        product.setLastModifiedDate(LocalDate.now());

        product.setViewCount(0L);
        product.setIsDeleted(request.getIsDeleted());
        log.info("Saving updated product with id: {}", id);
        try {
            Product savedProduct = productRepository.save(product);
            log.info("Successfully updated product with id: {}", id);
            return productMapper.toProductResponse(savedProduct);
        } catch (DataAccessException e) {
            log.error("Failed to update product with id: {}", id, e);
            throw new RuntimeException("Failed to update product in database", e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(String id) throws IOException {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsDeleted(true);
        product.setViewCount(0L);
        productRepository.save(product);
        //        List<String> imagePaths = product.getImages();
        //        if (imagePaths != null) {
        //            for (String imagePath : imagePaths) {
        //                try {
        //                    Files.deleteIfExists(Paths.get(imagePath));
        //                } catch (IOException e) {
        //                    throw new IOException("Failed to delete image: " + imagePath, e);
        //                }
        //            }
        //        }
        //        productRepository.delete(product);
    }
}
