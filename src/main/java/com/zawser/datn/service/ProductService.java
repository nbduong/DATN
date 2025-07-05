package com.zawser.datn.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zawser.datn.dto.request.ProductRequest;
import com.zawser.datn.dto.response.ProductResponse;
import com.zawser.datn.entity.*;
import com.zawser.datn.enums.DiscountType;
import com.zawser.datn.mapper.ProductMapper;
import com.zawser.datn.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;
    BrandRepository brandRepository;
    ProductPriceRepository productPriceRepository;
    DiscountRepository discountRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductRequest request) throws IOException {
        // Parse specifications JSON
        Map<String, String> specMap;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            specMap = objectMapper.readValue(
                    request.getSpecificationsJson(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new IOException("Failed to parse specifications JSON", e);
        }

        // Map ProductRequest to Product entity
        Product product = productMapper.toProduct(request);

        // Set Brand
        product.setBrand(brandRepository
                .findById(request.getBrandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + request.getBrandId())));

        // Set Category
        product.setCategory(categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId())));

        if (request.getDiscountId() != null) {
            Discount discount = discountRepository
                    .findById(request.getDiscountId())
                    .orElseThrow(() -> new EntityNotFoundException("Discount not found"));
            product.setDiscount(discount);
        } else {
            product.setDiscount(null); // Remove discount if not specified
        }

        // Handle product images
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

        // Handle specifications
        List<ProductSpecification> specs = specMap.entrySet().stream()
                .map(e -> new ProductSpecification(e.getKey(), e.getValue(), product))
                .collect(Collectors.toList());
        product.setSpecifications(specs);

        // Set audit fields
        product.setCreatedBy("ADMIN");
        product.setLastModifiedBy("ADMIN");
        product.setLastModifiedDate(LocalDateTime.now());
        product.setCreatedDate(LocalDateTime.now());
        product.setViewCount(0L);
        product.setPrices(new ArrayList<>());
        product.setIsDeleted(false);
        // Save product
        Product savedProduct = productRepository.save(product);

        // Create and save ProductPrice
        if (request.getSalePrice() != null) {
            ProductPrice price = ProductPrice.builder()
                    .product(savedProduct)
                    .salePrice(request.getSalePrice())
                    .startDate(LocalDateTime.now())
                    .priceType("REGULAR")
                    .isActive(true)
                    .createdBy("ADMIN")
                    .createdDate(LocalDateTime.now())
                    .build();
            productPriceRepository.save(price);
            savedProduct.getPrices().add(price);
            savedProduct = productRepository.save(savedProduct);
        } else {
            throw new IllegalArgumentException("Sale price is required");
        }
        ProductResponse response = productMapper.toProductResponse(savedProduct);
        response.setFinalPrice(calculateFinalPrice(savedProduct));
        return response;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductResponse response = productMapper.toProductResponse(product);
                    response.setFinalPrice(calculateFinalPrice(product));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setViewCount(1 + product.getViewCount());
        productRepository.save(product);

        ProductResponse response = productMapper.toProductResponse(product);
        response.setFinalPrice(calculateFinalPrice(product));
        return response;
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

        // Fetch existing product
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        // Update product fields
        productMapper.updateProduct(product, request);

        // Update Category
        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category with ID " + request.getCategoryId() + " not found"));
        product.setCategory(category);

        // Update Brand
        Brand brand = brandRepository
                .findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand with ID " + request.getBrandId() + " not found"));
        product.setBrand(brand);

        if (request.getDiscountId() != null) {
            Discount discount = discountRepository
                    .findById(request.getDiscountId())
                    .orElseThrow(() -> new EntityNotFoundException("Discount not found"));
            product.setDiscount(discount);
        } else {
            product.setDiscount(null); // Remove discount if not specified
        }

        Map<String, String> specMap;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            specMap = objectMapper.readValue(
                    request.getSpecificationsJson(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new IOException("Failed to parse specifications JSON", e);
        }

        // Update specifications
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

        // Update images
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

        // Update ProductPrice
        if (request.getSalePrice() != null) {
            // Deactivate existing active price
            product.getPrices().stream()
                    .filter(price -> Boolean.TRUE.equals(price.getIsActive()))
                    .forEach(price -> {
                        price.setIsActive(false);
                        price.setEndDate(LocalDateTime.now());
                        price.setLastModifiedBy("ADMIN");
                        price.setLastModifiedDate(LocalDateTime.now());
                        productPriceRepository.save(price);
                    });

            ProductPrice newPrice = ProductPrice.builder()
                    .product(product)
                    .salePrice(request.getSalePrice())
                    .startDate(LocalDateTime.now())
                    .priceType("REGULAR")
                    .isActive(true)
                    .createdBy("ADMIN")
                    .createdDate(LocalDateTime.now())
                    .lastModifiedBy("ADMIN")
                    .lastModifiedDate(LocalDateTime.now())
                    .build();
            productPriceRepository.save(newPrice);
            product.getPrices().add(newPrice);
        } else {
            throw new IllegalArgumentException("Sale price is required");
        }

        // Update audit fields
        product.setLastModifiedBy("ADMIN");
        product.setLastModifiedDate(LocalDateTime.now());
        product.setIsDeleted(request.getIsDeleted());

        // Save updated product
        try {
            product = productRepository
                    .findById(product.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found after save"));
            Product savedProduct = productRepository.save(product);
            ProductResponse response = productMapper.toProductResponse(savedProduct);
            response.setFinalPrice(calculateFinalPrice(savedProduct));
            return response;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update product in database", e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsDeleted(true);
        product.setViewCount(0L);
        productRepository.save(product);
    }

    public Double getProductFinalPrice(String productId) {
        Product product =
                productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        return calculateFinalPrice(product);
    }

    private Double calculateFinalPrice(Product product) {
        // Get the active sale price
        Double originalPrice = product.getPrices().stream()
                .filter(price -> Boolean.TRUE.equals(price.getIsActive()))
                .findFirst()
                .map(ProductPrice::getSalePrice)
                .orElse(0.0);

        // Get discount details
        Discount discount = product.getDiscount() != null
                ? product.getDiscount()
                : Discount.builder().build();
        DiscountType discountType = discount.getType();
        // Calculate final price based on discount type
        if (discountType == DiscountType.PERCENTAGE && discount.getDiscountPercent() != null) {
            return originalPrice * (1 - discount.getDiscountPercent() / 100);
        } else if (discountType == DiscountType.FIXED_AMOUNT && discount.getDiscountAmount() != null) {
            double finalPrice = originalPrice - discount.getDiscountAmount();
            return Math.max(finalPrice, 0.0); // Ensure final price is not negative
        }
        // Default to original price if discount type is invalid or values are null
        return originalPrice;
    }
}
