package edu.unimagdalena.productservice.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import edu.unimagdalena.productservice.model.Product;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductByName(String name);
    Optional<Product> getProductsByCategory(String category);
    Product addProduct(Product product);
    Optional<Product> updateProduct(UUID uuid, Product product);
    void deleteProduct(UUID uuid);
}
