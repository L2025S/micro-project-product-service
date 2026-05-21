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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        ProductRequestDTO productRequestDTO = new ProductRequestDTO(
        "Wireless Noise-Cancelling Headphones",
        new BigDecimal("5999.99"),
        "Lightweight over-ear Bluetooth headphones with" +
                " active noise cancellation,30-hour battery life, " +
                "and fast charging support.",
        250);


        Product product = Product.create(
                productRequestDTO.name(),
                productRequestDTO.description(),
                productRequestDTO.price(),
                productRequestDTO.stock()
        );


        Product saved = Product.create(
                productRequestDTO.name(),
                productRequestDTO.description(),
                productRequestDTO.price(),
                productRequestDTO.stock()
        );

        when(saved.getUuid()).thenReturn("abc-123");
        when(saved.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(saved.getUpdatedAt()).thenReturn(LocalDateTime.now());

        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "abc-123",
                productRequestDTO.name(),
                productRequestDTO.price(),
                productRequestDTO.description(),
                productRequestDTO.stock(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );

        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(productMapper.toResponseDTO(saved)).thenReturn(productResponseDTO);


        //Act
        ProductResponseDTO result = productService.create(productRequestDTO);

        //Assert
        assertEquals("abc-123", result.uuid());
        assertEquals(productRequestDTO.name(), result.name());
        assertEquals(productRequestDTO.price(), result.price());
        assertEquals(productRequestDTO.description(),result.description());
        assertEquals(productRequestDTO.stock(), result.stock());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());

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