package edu.unimagdalena.productservice.service;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(UUID id);
    Product createProduct(Product product);
    Optional<Product> updateProduct(UUID id, Product productDetails);
    void deleteProduct(UUID id);
}


