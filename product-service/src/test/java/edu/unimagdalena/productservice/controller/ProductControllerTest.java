package edu.unimagdalena.productservice.controller;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ProductControllerTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ProductRepository productRepository;

    private WebTestClient webTestClient;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .build();

        testProduct = productRepository.save(testProduct);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Act & Assert
        webTestClient.get()
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class)
                .value(products -> {
                    assertThat(products).hasSize(1);
                    Product returnedProduct = products.get(0);
                    assertThat(returnedProduct.getId()).isEqualTo(testProduct.getId());
                    assertThat(returnedProduct.getName()).isEqualTo(testProduct.getName());
                    assertThat(returnedProduct.getPrice()).isEqualByComparingTo(testProduct.getPrice());
                    assertThat(returnedProduct.getCategory()).isEqualTo(testProduct.getCategory());
                });
    }

    @Test
    void getProductById_WithExistingId_ShouldReturnProduct() {
        // Act & Assert
        webTestClient.get()
                .uri("/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .value(returnedProduct -> {
                    assertThat(returnedProduct.getId()).isEqualTo(testProduct.getId());
                    assertThat(returnedProduct.getName()).isEqualTo(testProduct.getName());
                    assertThat(returnedProduct.getPrice()).isEqualByComparingTo(testProduct.getPrice());
                    assertThat(returnedProduct.getCategory()).isEqualTo(testProduct.getCategory());
                });
    }

    @Test
    void getProductById_WithNonExistingId_ShouldReturnNotFound() {
        // Act & Assert
        webTestClient.get()
                .uri("/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        // Arrange
        Product newProduct = Product.builder()
                .name("New Product")
                .price(new BigDecimal("49.99"))
                .category("Books")
                .build();

        // Act & Assert
        webTestClient.post()
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Product.class)
                .value(product -> {
                    assertThat(product.getId()).isNotNull();
                    assertThat(product.getName()).isEqualTo(newProduct.getName());
                    assertThat(product.getPrice()).isEqualByComparingTo(newProduct.getPrice());
                    assertThat(product.getCategory()).isEqualTo(newProduct.getCategory());
                });
    }

    @Test
    void updateProduct_WithExistingId_ShouldReturnUpdatedProduct() {
        // Arrange
        Product productUpdate = Product.builder()
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/{id}", testProduct.getId())
                .bodyValue(productUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class)
                .value(product -> {
                    assertThat(product.getId()).isEqualTo(testProduct.getId());
                    assertThat(product.getName()).isEqualTo("Updated Product");
                    assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("129.99"));
                    assertThat(product.getCategory()).isEqualTo(testProduct.getCategory());
                });
    }

    @Test
    void updateProduct_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        Product productUpdate = Product.builder()
                .name("Updated Product")
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/{id}", UUID.randomUUID())
                .bodyValue(productUpdate)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteProduct_ShouldRemoveProduct() {
        // Act & Assert
        webTestClient.delete()
                .uri("/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify product was deleted
        webTestClient.get()
                .uri("/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isNotFound();
    }
}

