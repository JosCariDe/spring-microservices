package edu.unimagdalena.inventoryservice.controller;

import edu.unimagdalena.inventoryservice.model.Inventory;
import edu.unimagdalena.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Flux<Inventory> getAllInventories() {
        return Flux.fromIterable(inventoryService.getAllInventories());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Inventory>> getInventoryById(@PathVariable UUID id) {
        return Mono.justOrEmpty(inventoryService.getInventoryById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Inventory>> createInventory(@RequestBody Inventory inventory) {
        return Mono.just(inventoryService.createInventory(inventory))
                .map(savedInventory -> ResponseEntity.status(HttpStatus.CREATED).body(savedInventory));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Inventory>> updateInventory(@PathVariable UUID id, @RequestBody Inventory inventory) {
        return Mono.justOrEmpty(inventoryService.updateInventory(id, inventory))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteInventory(@PathVariable UUID id) {
        inventoryService.deleteInventory(id);
        return Mono.just(ResponseEntity.noContent().build());
    }
}
