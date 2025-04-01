package edu.unimagdalena.productservice.repository;

import edu.unimagdalena.productservice.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
public class ProductRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
    }

    @Test
    void saveProduct_ShouldPersistProduct() {
        String id = UUID.randomUUID().toString();
        // Arrange
        Product product = Product.builder()
                .id(id)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .build();

        // Act
        Product savedProduct = productRepository.save(product);

        // Assert
        assertThat(savedProduct.getId()).isEqualTo(product.getId());
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(savedProduct.getCategory()).isEqualTo("Electronics");
    }

    @Test
    void findById_WithExistingId_ShouldReturnProduct() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .id(id)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .build();

        Product savedProduct = productRepository.save(product);

        // Act
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        String id = UUID.randomUUID().toString();
        Optional<Product> foundProduct = productRepository.findById(id);

        // Assert
        assertThat(foundProduct).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllProducts() {
        // Arrange
        productRepository.deleteAll(); // Ensure clean state
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        Product product1 = Product.builder()
                .id(id1)
                .name("Product 1")
                .price(new BigDecimal("49.99"))
                .category("Books")
                .build();

        Product product2 = Product.builder()
                .id(id2)
                .name("Product 2")
                .price(new BigDecimal("149.99"))
                .category("Electronics")
                .build();

        productRepository.saveAll(Arrays.asList(product1, product2));

        // Act
        List<Product> products = productRepository.findAll();

        // Assert
        assertThat(products).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveProduct() {
        // Arrange
        String id1 = UUID.randomUUID().toString();
        Product product = Product.builder()
                .id(id1)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .build();

        Product savedProduct = productRepository.save(product);

        // Act
        productRepository.deleteById(savedProduct.getId());
        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(deletedProduct).isEmpty();
    }
}

