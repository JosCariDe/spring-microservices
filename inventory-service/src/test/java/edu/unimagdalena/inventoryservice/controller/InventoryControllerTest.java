package edu.unimagdalena.inventoryservice.controller;

import edu.unimagdalena.inventoryservice.model.Inventory;
import edu.unimagdalena.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class InventoryControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private InventoryRepository inventoryRepository;

    private WebTestClient webTestClient;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        testInventory = Inventory.builder()
                .products(productIds)
                .build();

        testInventory = inventoryRepository.save(testInventory);
    }

    @AfterEach
    void tearDown() {
        inventoryRepository.deleteAll();
    }

    @Test
    void getAllInventories_ShouldReturnAllInventories() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/inventory")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Inventory.class)
                .value(inventories -> {
                    assertThat(inventories).hasSize(1);
                    Inventory returnedInventory = inventories.get(0);
                    assertThat(returnedInventory.getId()).isEqualTo(testInventory.getId());
                    assertThat(returnedInventory.getProducts()).isEqualTo(testInventory.getProducts());
                });
    }

    @Test
    void getInventoryById_WithExistingId_ShouldReturnInventory() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/inventory/{id}", testInventory.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Inventory.class)
                .value(returnedInventory -> {
                    assertThat(returnedInventory.getId()).isEqualTo(testInventory.getId());
                    assertThat(returnedInventory.getProducts()).isEqualTo(testInventory.getProducts());
                });
    }

    @Test
    void getInventoryById_WithNonExistingId_ShouldReturnNotFound() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/inventory/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createInventory_ShouldReturnCreatedInventory() {
        // Arrange
        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Inventory newInventory = Inventory.builder()
                .products(productIds)
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/inventory")
                .bodyValue(newInventory)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Inventory.class)
                .value(inventory -> {
                    assertThat(inventory.getId()).isNotNull();
                    assertThat(inventory.getProducts()).isEqualTo(productIds);
                });
    }

    @Test
    void updateInventory_WithExistingId_ShouldReturnUpdatedInventory() {
        // Arrange
        List<UUID> updatedProductIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Inventory inventoryUpdate = Inventory.builder()
                .products(updatedProductIds)
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/inventory/{id}", testInventory.getId())
                .bodyValue(inventoryUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Inventory.class)
                .value(inventory -> {
                    assertThat(inventory.getId()).isEqualTo(testInventory.getId());
                    assertThat(inventory.getProducts()).isEqualTo(updatedProductIds);
                });
    }

    @Test
    void updateInventory_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        List<UUID> updatedProductIds = Arrays.asList(UUID.randomUUID());
        Inventory inventoryUpdate = Inventory.builder()
                .products(updatedProductIds)
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/inventory/{id}", UUID.randomUUID())
                .bodyValue(inventoryUpdate)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteInventory_ShouldRemoveInventory() {
        // Act & Assert
        webTestClient.delete()
                .uri("/api/inventory/{id}", testInventory.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify inventory was deleted
        webTestClient.get()
                .uri("/api/inventory/{id}", testInventory.getId())
                .exchange()
                .expectStatus().isNotFound();
    }
}

