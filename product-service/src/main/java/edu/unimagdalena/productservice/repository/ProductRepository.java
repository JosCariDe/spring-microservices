package edu.unimagdalena.productservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.unimagdalena.productservice.model.Product;

public interface ProductRepository extends MongoRepository<Product,UUID>{
    Optional<Product> findByName(String name);  
    Optional<Product> findByCategory(String category);
}
