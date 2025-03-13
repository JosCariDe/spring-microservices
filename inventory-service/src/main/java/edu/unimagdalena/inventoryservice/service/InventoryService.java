package edu.unimagdalena.inventoryservice.service;

import edu.unimagdalena.inventoryservice.model.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryService {
    List<Inventory> getAllInventories();
    Optional<Inventory> getInventoryById(UUID id);
    Inventory createInventory(Inventory inventory);
    Optional<Inventory> updateInventory(UUID id, Inventory inventoryDetails);
    void deleteInventory(UUID id);
}

