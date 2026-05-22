package se.iths.lw.microprojectproductservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.exception.ProductNotFoundException;
import se.iths.lw.microprojectproductservice.mapper.ProductMapper;
import se.iths.lw.microprojectproductservice.model.Product;
import se.iths.lw.microprojectproductservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    void create_shouldSaveProductAndReturnProductResponseDTO() {

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

        //Assert
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
    void findById_shouldReturnResponseDTO_whenProductExists() {

        //prepare
        Long id = 1L;
        Product product = mock(Product.class);
        ProductResponseDTO response = mock(ProductResponseDTO.class);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(response);

        // Act
        ProductResponseDTO result = productService.findById(id);

        // Assert
        assertEquals(response, result);
        verify(productRepository).findById(id);
        verify(productMapper).toResponseDTO(product);
    }

    @Test
    void findById_shouldThrowException_whenProductNotFound() {

        //Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        //Act & Assert
        assertThrows(ProductNotFoundException.class,
                ()->productService.findById(1L));
    }


    @Test
    void findAll() {

        //Arrange
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);

        ProductResponseDTO r1 = mock(ProductResponseDTO.class);
        ProductResponseDTO r2 = mock(ProductResponseDTO.class);

        when(productRepository.findAll()).thenReturn(List.of(p1,p2));
        when(productMapper.toResponseDTO(p1)).thenReturn(r1);
        when(productMapper.toResponseDTO(p2)).thenReturn(r2);

        // Act
        List<ProductResponseDTO> result = productService.findAll();

        //Assert
        assertEquals(2, result.size());
        assertTrue(result.containsAll((List.of(r1,r2))));
    }

    @Test
    void findByUuid_shouldReturnResponseDTO_whenProductExists() {

        // Arrange
        String uuid = "abc-123";
        Product product = mock(Product.class);
        ProductResponseDTO response = mock(ProductResponseDTO.class);

        when(productRepository.findByUuid(uuid)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(response);

        // Act
        ProductResponseDTO result = productService.findByUuid(uuid);

        // Assert
        assertEquals(response, result);

    }

    @Test
    void findByUuid_shouldThrowException_whenProductNotFound(){
        // Arrange
        when(productRepository.findByUuid("abc")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                ()-> productService.findByUuid("abc"));


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