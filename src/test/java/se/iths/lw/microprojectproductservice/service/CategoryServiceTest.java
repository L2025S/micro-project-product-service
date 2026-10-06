package se.iths.lw.microprojectproductservice.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.iths.lw.microprojectproductservice.dto.CategoryRequestDTO;
import se.iths.lw.microprojectproductservice.dto.CategoryResponseDTO;
import se.iths.lw.microprojectproductservice.exception.CategoryAlreadyExistsException;
import se.iths.lw.microprojectproductservice.mapper.CategoryMapper;
import se.iths.lw.microprojectproductservice.model.Category;
import se.iths.lw.microprojectproductservice.repository.CategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CategoryMapper categoryMapper;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void create_shouldSaveCategoryAndReturnResponseDTO() {
        // Arrange
        CategoryRequestDTO request = new CategoryRequestDTO("Shoes");
        CategoryResponseDTO response = new CategoryResponseDTO(1L, "Shoes");

        when(categoryRepository.existsByNameIgnoreCase("Shoes")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation ->invocation.getArgument(0));
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(response);

        // Act
        CategoryResponseDTO result = categoryService.create(request);

        // Assert
        assertEquals( response, result);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals("Shoes", captor.getValue().getName());
    }


    @Test
    void create_shouldTrimTheName() {

        // Arrange
        CategoryRequestDTO request = new CategoryRequestDTO (" Hats ");
        CategoryResponseDTO response =  new CategoryResponseDTO(2L, "Hats");

        when(categoryRepository.existsByNameIgnoreCase("Hats")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(response);

        // Act
        categoryService.create(request);

        // Assert: the duplicate check and the saved name both use the trimmed value
        verify(categoryRepository).existsByNameIgnoreCase("Hats");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals("Hats", captor.getValue().getName());


    }


    @Test
    void create_shouldThrow_whenNameAlreadyExists() {
        // Arrange
        CategoryRequestDTO request = new CategoryRequestDTO ("Shoes");
        when(categoryRepository.existsByNameIgnoreCase("Shoes")).thenReturn(true);

        // Act & Assert
        assertThrows(CategoryAlreadyExistsException.class,
                ()-> categoryService.create(request));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void findAll_shouldReturnMappedList() {
        // Arrange
        Category shoes = Category.create("Shoes");
        Category hats = Category.create("Hats");
        CategoryResponseDTO shoesDto = new CategoryResponseDTO(1L, "Shoes");
        CategoryResponseDTO hatsDto = new CategoryResponseDTO(2L, "Hats");

        when(categoryRepository.findAll()).thenReturn(List.of(shoes, hats));
        when(categoryMapper.toResponseDTO(shoes)).thenReturn(shoesDto);
        when(categoryMapper.toResponseDTO(hats)).thenReturn(hatsDto);


        // Act
        List<CategoryResponseDTO> result = categoryService.findAll();

        // Assert
        assertEquals(List.of(shoesDto, hatsDto), result);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoCategoriesExists() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        assertTrue(categoryService.findAll().isEmpty());
    }
}
