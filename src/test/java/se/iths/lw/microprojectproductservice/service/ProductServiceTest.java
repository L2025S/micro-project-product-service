package se.iths.lw.microprojectproductservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.mapper.ProductMapper;
import se.iths.lw.microprojectproductservice.model.Product;
import se.iths.lw.microprojectproductservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    ProductMapper productMapper;
    @InjectMocks
    ProductService productService;



    @Test
    void create() {

        //Arrange
        ProductRequestDTO request = new ProductRequestDTO(
        "Wireless Noise-Cancelling Headphones",
        new BigDecimal("5999.99"),
        "Lightweight over-ear Bluetooth headphones with" +
                " active noise cancellation,30-hour battery life, " +
                "and fast charging support.",
        250);

        Product saved = mock(Product.class);

        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        ProductResponseDTO response = new ProductResponseDTO(
                "uuid-123",
                request.name(),
                request.price(),
                request.description(),
                request.stock(),
                createdAt,
                updatedAt
        );

        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponseDTO(saved)).thenReturn(response);

        // Act
        ProductResponseDTO result = productService.create(request);

        assertEquals("uuid-123", result.uuid());
        assertEquals(request.name(),result.name());
        assertEquals(request.price(), result.price());
        assertEquals(request.description(), result.description());
        assertEquals(request.stock(), result.stock());
        assertEquals(createdAt,result.createdAt());
        assertEquals(updatedAt, result.updatedAt());

        verify(productRepository).save(any(Product.class));
        verify(productMapper).toResponseDTO(saved);





    }

    @Test
    void findById() {
    }

    @Test
    void findAll() {
    }

    @Test
    void findByUuid() {
    }

    @Test
    void testFindAll() {
    }

    @Test
    void reduceStock() {
    }

    @Test
    void increaseStock() {
    }

    @Test
    void updateBasicInfo() {
    }

    @Test
    void deleteById() {
    }

    @Test
    void deleteByUuid() {
    }

    @Test
    void decreaseStockBatch() {
    }
}