package edu.unimagdalena.productservice.service;

import edu.unimagdalena.productservice.model.Product;
import edu.unimagdalena.productservice.repository.ProductRepository;
import edu.unimagdalena.productservice.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private String productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID().toString();
        ;
        product = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        String id = UUID.randomUUID().toString();
        List<Product> products = Arrays.asList(product, Product.builder().id(id).build());
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(products);
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WithExistingId_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        Optional<Product> result = productService.getProductById(productId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        String nonExistingId = UUID.randomUUID().toString();

        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(nonExistingId);

        // Assert
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void createProduct_WithoutId_ShouldGenerateIdAndSaveProduct() {
        // Arrange
        Product newProduct = Product.builder()
                .name("New Product")
                .price(new BigDecimal("49.99"))
                .category("Books")
                .build();

        String id = UUID.randomUUID().toString();
        Product savedProduct = Product.builder()
                .id(id)
                .name(newProduct.getName())
                .price(newProduct.getPrice())
                .category(newProduct.getCategory())
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product result = productService.createProduct(newProduct);

        // Assert
        assertThat(result).isEqualTo(savedProduct);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WithId_ShouldUseProvidedIdAndSaveProduct() {
        // Arrange
        Product newProduct = Product.builder()
                .id(productId)
                .name("New Product")
                .price(new BigDecimal("49.99"))
                .category("Books")
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        // Act
        Product result = productService.createProduct(newProduct);

        // Assert
        assertThat(result).isEqualTo(newProduct);
        assertThat(result.getId()).isEqualTo(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WithExistingId_ShouldUpdateAndReturnProduct() {
        // Arrange
        Product productToUpdate = Product.builder()
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .category(product.getCategory())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Optional<Product> result = productService.updateProduct(productId, productToUpdate);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Updated Product");
        assertThat(result.get().getPrice()).isEqualTo(new BigDecimal("129.99"));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        String nonExistingId = UUID.randomUUID().toString();

        Product productToUpdate = Product.builder()
                .name("Updated Product")
                .build();

        when(productRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.updateProduct(nonExistingId, productToUpdate);

        // Assert
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(nonExistingId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldCallRepositoryDelete() {
        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository, times(1)).deleteById(productId);
    }
}

