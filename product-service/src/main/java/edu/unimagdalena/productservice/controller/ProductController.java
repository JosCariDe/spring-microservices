package edu.unimagdalena.productservice.controller;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.service.ProductService;
import edu.unimagdalena.productservice.service.caching.CacheInvalidationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class ProductController {


    private final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final CacheInvalidationService cacheInvalidationService;

    @GetMapping
    public ResponseEntity<Flux<Product>> getAllProducts() {
        logger.info("Get all products");
        return ResponseEntity.ok()
                .header("X-Cache", "ORIGIN")
                .body(Flux.fromIterable(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable String id) throws InterruptedException {

        if (id.equals("550e8400-e29b-41d4-a716-446655440010")) {
            throw new IllegalStateException("PRODUCTO NO ENCONTRADO");
        }



        if (id.equals("550e8400-e29b-41d4-a716-446655440007")) {
            TimeUnit.SECONDS.sleep(5L); //5 Segundo de demora Para abrir CircuitBraker
        }

        logger.info("Get product by id: {}", id);
        return Mono.justOrEmpty(productService.getProductById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Product>> createProduct(@RequestBody Product product) {
        return Mono.just(productService.createProduct(product))
                .map(savedProduct -> ResponseEntity.status(HttpStatus.CREATED).body(savedProduct));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return Mono.justOrEmpty(productService.updateProduct(id, product))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return Mono.just(ResponseEntity.noContent().build());
    }
}
