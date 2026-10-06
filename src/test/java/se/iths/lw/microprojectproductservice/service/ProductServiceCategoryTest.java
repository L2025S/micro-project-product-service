package se.iths.lw.microprojectproductservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.exception.CategoryNotFoundException;
import se.iths.lw.microprojectproductservice.exception.ProductNotFoundException;
import se.iths.lw.microprojectproductservice.mapper.ProductMapper;
import se.iths.lw.microprojectproductservice.model.Category;
import se.iths.lw.microprojectproductservice.model.Product;
import se.iths.lw.microprojectproductservice.repository.CategoryRepository;
import se.iths.lw.microprojectproductservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// unit tests for the category parts of ProductService

@ExtendWith(MockitoExtension.class)
class ProductServiceCategoryTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    ProductMapper productMapper;
    @Mock
    CategoryRepository categoryRepository;
    @InjectMocks
    ProductService productService;

    // ============================== create ==============================

    @Test
    void create_shouldSaveProductWithCategory_whenCategoryIdIsGiven() {
        // Arrange
        Category category = Category.create("Shoes");
        ProductRequestDTO request = new ProductRequestDTO(
                "Sneaker", null, new BigDecimal("99.99"), "Description", 10, 1L);
        ProductResponseDTO response = mock(ProductResponseDTO.class);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponseDTO(any(Product.class))).thenReturn(response);

        // Act
        ProductResponseDTO result = productService.create(request);

        // Assert
        assertSame(response, result);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertSame(category, captor.getValue().getCategory());
    }

    @Test
    void create_shouldSaveProductWithoutCategory_whenCategoryIdIsNull() {
        // Arrange
        ProductRequestDTO request = new ProductRequestDTO(
                "Sneaker", null, new BigDecimal("99.99"), "Description", 10, null);
        ProductResponseDTO response = mock(ProductResponseDTO.class);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponseDTO(any(Product.class))).thenReturn(response);

        // Act
        productService.create(request);

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertNull(captor.getValue().getCategory());
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void create_shouldThrow_whenCategoryDoesNotExist() {
        // Arrange
        ProductRequestDTO request = new ProductRequestDTO(
                "Sneaker", null, new BigDecimal("99.99"), "Description", 10, 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class,
                () -> productService.create(request));

        verify(productRepository, never()).save(any(Product.class));
    }

    // ========================== findByCategoryId ==========================

    @Test
    void findByCategoryId_shouldReturnMappedProducts() {
        // Arrange
        Category category = Category.create("Shoes");
        Product product = mock(Product.class);
        ProductResponseDTO response = mock(ProductResponseDTO.class);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(response);

        // Act
        List<ProductResponseDTO> result = productService.findByCategoryId(1L);

        // Assert
        assertEquals(List.of(response), result);
    }

    @Test
    void findByCategoryId_shouldReturnEmptyList_whenCategoryHasNoProducts() {
        // Arrange
        Category category = Category.create("Hats");
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryId(2L)).thenReturn(List.of());

        // Act & Assert
        assertTrue(productService.findByCategoryId(2L).isEmpty());
    }

    @Test
    void findByCategoryId_shouldThrow_whenCategoryDoesNotExist() {
        // Arrange
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class,
                () -> productService.findByCategoryId(99L));

        verify(productRepository, never()).findByCategoryId(anyLong());
    }

    // ============================ updateCategory ============================

    @Test
    void updateCategory_shouldChangeCategoryAndReturnResponse() {
        // Arrange
        Product product = mock(Product.class);
        Product saved = mock(Product.class);
        Category category = Category.create("Hats");
        ProductResponseDTO response = mock(ProductResponseDTO.class);

        when(productRepository.findByUuid("abc")).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(saved);
        when(productMapper.toResponseDTO(saved)).thenReturn(response);

        // Act
        ProductResponseDTO result = productService.updateCategory("abc", 2L);

        // Assert
        assertEquals(response, result);
        verify(product).changeCategory(category);
    }

    @Test
    void updateCategory_shouldThrow_whenProductDoesNotExist() {
        // Arrange
        when(productRepository.findByUuid("abc")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.updateCategory("abc", 1L));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateCategory_shouldThrow_whenCategoryDoesNotExist() {
        // Arrange
        Product product = mock(Product.class);
        when(productRepository.findByUuid("abc")).thenReturn(Optional.of(product));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class,
                () -> productService.updateCategory("abc", 99L));

        verify(product, never()).changeCategory(any(Category.class));
        verify(productRepository, never()).save(any(Product.class));
    }
}