package edu.unimagdalena.productservice.service;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.repository.ProductRepository;
import edu.unimagdalena.productservice.service.ProductService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @PostConstruct
    public void initDatabaseIfEmpty() {
        if (productRepository.count() == 0) {
            List<Product> initialProducts = Arrays.asList(
                    new Product("550e8400-e29b-41d4-a716-446655440002", "Laptop", new BigDecimal("999.99"), "Electronics"),
                    new Product("550e8400-e29b-41d4-a716-446655440003", "Headphones", new BigDecimal("49.99"), "Accessories"),
                    new Product("550e8400-e29b-41d4-a716-446655440006", "Smartphone", new BigDecimal("699.99"), "Electronics")
            );
            productRepository.saveAll(initialProducts);
            System.out.println("Productos iniciales insertados.");
        } else {
            System.out.println("La base de datos ya tiene productos.");
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(Product product) {

        return productRepository.save(product);
    }

    @Override
    public Optional<Product> updateProduct(String id, Product productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (productDetails.getName() != null) {
                        existingProduct.setName(productDetails.getName());
                    }
                    if (productDetails.getPrice() != null) {
                        existingProduct.setPrice(productDetails.getPrice());
                    }
                    if (productDetails.getCategory() != null) {
                        existingProduct.setCategory(productDetails.getCategory());
                    }
                    return productRepository.save(existingProduct);
                });
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}

