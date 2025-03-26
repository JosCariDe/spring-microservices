package edu.unimagdalena.productservice.controller;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/getall")
    public Flux<Product> getAllProducts() {
        return Flux.fromIterable(productService.getAllProducts());
    }

    @GetMapping("/getby{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable UUID id) {
        return Mono.justOrEmpty(productService.getProductById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return Mono.just(productService.createProduct(product))
                .map(savedProduct -> ResponseEntity.status(HttpStatus.CREATED).body(savedProduct));
    }

    @PutMapping("/updateby{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable UUID id, @RequestBody Product product) {
        return Mono.justOrEmpty(productService.updateProduct(id, product))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/deleteby{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return Mono.just(ResponseEntity.noContent().build());
    }
}

