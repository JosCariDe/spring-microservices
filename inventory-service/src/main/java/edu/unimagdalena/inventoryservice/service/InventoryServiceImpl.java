package edu.unimagdalena.inventoryservice.service;

import edu.unimagdalena.inventoryservice.config.CacheConfig;
import edu.unimagdalena.inventoryservice.model.Inventory;
import edu.unimagdalena.inventoryservice.repository.InventoryRepository;
import edu.unimagdalena.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    public static final String INVENTORY_CACHE = "inventory";
    private final InventoryRepository inventoryRepository;

    @Override
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    @Cacheable(value = INVENTORY_CACHE, key = "#id")
    public Optional<Inventory> getInventoryById(UUID id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    @CachePut(value = INVENTORY_CACHE, key = "#id")
    public Optional<Inventory> updateInventory(UUID id, Inventory inventoryDetails) {
        return inventoryRepository.findById(id)
                .map(existingInventory -> {
                    if (inventoryDetails.getProducts() != null) {
                        existingInventory.setProducts(inventoryDetails.getProducts());
                    }
                    return inventoryRepository.save(existingInventory);
                });
    }

    @Override
    public void deleteInventory(UUID id) {
        inventoryRepository.deleteById(id);
    }
}
