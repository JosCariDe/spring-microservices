package edu.unimagdalena.inventoryservice.service;

import edu.unimagdalena.inventoryservice.model.Inventory;
import edu.unimagdalena.inventoryservice.repository.InventoryRepository;
import edu.unimagdalena.inventoryservice.service.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private UUID inventoryId;
    private List<UUID> productIds;

    @BeforeEach
    void setUp() {
        inventoryId = UUID.randomUUID();
        productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        inventory = Inventory.builder()
                .id(inventoryId)
                .products(productIds)
                .build();
    }

    @Test
    void getAllInventories_ShouldReturnAllInventories() {
        // Arrange
        List<Inventory> inventories = Arrays.asList(inventory, Inventory.builder().id(UUID.randomUUID()).build());
        when(inventoryRepository.findAll()).thenReturn(inventories);

        // Act
        List<Inventory> result = inventoryService.getAllInventories();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(inventories);
        verify(inventoryRepository, times(1)).findAll();
    }

    @Test
    void getInventoryById_WithExistingId_ShouldReturnInventory() {
        // Arrange
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));

        // Act
        Optional<Inventory> result = inventoryService.getInventoryById(inventoryId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(inventory);
        verify(inventoryRepository, times(1)).findById(inventoryId);
    }

    @Test
    void getInventoryById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(inventoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Inventory> result = inventoryService.getInventoryById(nonExistingId);

        // Assert
        assertThat(result).isEmpty();
        verify(inventoryRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void createInventory_ShouldSaveAndReturnInventory() {
        // Arrange
        List<UUID> newProductIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Inventory newInventory = Inventory.builder()
                .products(newProductIds)
                .build();

        Inventory savedInventory = Inventory.builder()
                .id(UUID.randomUUID())
                .products(newProductIds)
                .build();

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

        // Act
        Inventory result = inventoryService.createInventory(newInventory);

        // Assert
        assertThat(result).isEqualTo(savedInventory);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WithExistingId_ShouldUpdateAndReturnInventory() {
        // Arrange
        List<UUID> updatedProductIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Inventory inventoryToUpdate = Inventory.builder()
                .products(updatedProductIds)
                .build();

        Inventory updatedInventory = Inventory.builder()
                .id(inventoryId)
                .products(updatedProductIds)
                .build();

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(updatedInventory);

        // Act
        Optional<Inventory> result = inventoryService.updateInventory(inventoryId, inventoryToUpdate);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProducts()).isEqualTo(updatedProductIds);
        verify(inventoryRepository, times(1)).findById(inventoryId);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        List<UUID> updatedProductIds = Arrays.asList(UUID.randomUUID());
        Inventory inventoryToUpdate = Inventory.builder()
                .products(updatedProductIds)
                .build();

        when(inventoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Inventory> result = inventoryService.updateInventory(nonExistingId, inventoryToUpdate);

        // Assert
        assertThat(result).isEmpty();
        verify(inventoryRepository, times(1)).findById(nonExistingId);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void deleteInventory_ShouldCallRepositoryDelete() {
        // Act
        inventoryService.deleteInventory(inventoryId);

        // Assert
        verify(inventoryRepository, times(1)).deleteById(inventoryId);
    }
}

