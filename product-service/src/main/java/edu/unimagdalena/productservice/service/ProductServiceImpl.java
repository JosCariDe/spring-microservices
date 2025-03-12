package edu.unimagdalena.productservice.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private final ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public Optional<Product> getProductsByCategory(String category) {
        return productRepository.findByName(category);
    }

    @Override
    public Product addProduct(Product product) {
        //return productRepository.insert(product);
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> updateProduct(UUID uuid, Product productNew) {
        return productRepository.findById(uuid).map(existingProduct->{
            if(productNew.getName()!=null) existingProduct.setName(productNew.getName());
            if(productNew.getPrice()!=null) existingProduct.setPrice(productNew.getPrice());
            if(productNew.getCategory()!=null) existingProduct.setCategory(productNew.getCategory());
            if(productNew.getStock()!=null) existingProduct.setStock(productNew.getStock());

            return productRepository.save(existingProduct);
        });
    }

    @Override
    public void deleteProduct(UUID uuid) {
        if(productRepository.findById(uuid)!=null){
            productRepository.deleteById(uuid);
        }
    }
}