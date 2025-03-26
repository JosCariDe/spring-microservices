package edu.unimagdalena.productservice.service;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.repository.ProductRepository;
import edu.unimagdalena.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    //@Autowired
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Cacheable(value = "product")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }
        return productRepository.save(product);
    }

    @Override
    @CachePut(value = "product", key = "#id")
    public Optional<Product> updateProduct(UUID id, Product productDetails) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    if (productDetails.getName() != null) {
                        existingProduct.setName(productDetails.getName());
                    }
                    if (productDetails.getPrice() != null) {
                        existingProduct.setPrice(productDetails.getPrice());
                    }
                    if (productDetails.getCategory() != null) {
                        existingProduct.setCategory(productDetails.getCategory());
                    }
                    return productRepository.save(existingProduct);
                });
    }

    @Override
    @CacheEvict(value = "product", key = "#id") // Elimina el producto de la caché al borrarlo
    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }
}

