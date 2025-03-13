package edu.unimagdalena.inventoryservice.repository;

import edu.unimagdalena.inventoryservice.model.Inventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InventoryRepositoryTest {

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

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void saveInventory_ShouldPersistInventory() {
        // Arrange
        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        Inventory inventory = Inventory.builder()
                .products(productIds)
                .build();

        // Act
        Inventory savedInventory = inventoryRepository.save(inventory);

        // Assert
        assertThat(savedInventory.getId()).isNotNull();
        assertThat(savedInventory.getProducts()).hasSize(2);
        assertThat(savedInventory.getProducts()).isEqualTo(productIds);
    }

    @Test
    void findById_WithExistingId_ShouldReturnInventory() {
        // Arrange
        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        Inventory inventory = Inventory.builder()
                .products(productIds)
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Act
        Optional<Inventory> foundInventory = inventoryRepository.findById(savedInventory.getId());

        // Assert
        assertThat(foundInventory).isPresent();
        assertThat(foundInventory.get().getId()).isEqualTo(savedInventory.getId());
        assertThat(foundInventory.get().getProducts()).isEqualTo(productIds);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<Inventory> foundInventory = inventoryRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundInventory).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllInventories() {
        // Arrange
        inventoryRepository.deleteAll(); // Ensure clean state

        List<UUID> productIds1 = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        Inventory inventory1 = Inventory.builder()
                .products(productIds1)
                .build();

        List<UUID> productIds2 = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Inventory inventory2 = Inventory.builder()
                .products(productIds2)
                .build();

        inventoryRepository.saveAll(Arrays.asList(inventory1, inventory2));

        // Act
        List<Inventory> inventories = inventoryRepository.findAll();

        // Assert
        assertThat(inventories).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveInventory() {
        // Arrange
        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        Inventory inventory = Inventory.builder()
                .products(productIds)
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Act
        inventoryRepository.deleteById(savedInventory.getId());
        Optional<Inventory> deletedInventory = inventoryRepository.findById(savedInventory.getId());

        // Assert
        assertThat(deletedInventory).isEmpty();
    }
}

