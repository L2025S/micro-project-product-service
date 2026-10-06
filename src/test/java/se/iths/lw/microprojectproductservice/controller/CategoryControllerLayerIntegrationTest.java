package se.iths.lw.microprojectproductservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.iths.lw.microprojectproductservice.dto.CategoryRequestDTO;
import se.iths.lw.microprojectproductservice.dto.CategoryResponseDTO;
import se.iths.lw.microprojectproductservice.exception.CategoryAlreadyExistsException;
import se.iths.lw.microprojectproductservice.service.CategoryService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ NEW FILE: controller-layer tests for CategoryController (same style as ProductControllerLayerIntegrationTest).
@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerLayerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    // ============================ READ =============================================

    @Test
    void getAllCategories_shouldReturnList_withoutLogin() throws Exception {
        // Arrange
        when(categoryService.findAll()).thenReturn(List.of(
                new CategoryResponseDTO(1L, "Shoes"),
                new CategoryResponseDTO(2L, "Hats")));

        // Act & Assert
        mockMvc.perform(get("/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Shoes"))
                .andExpect(jsonPath("$[1].name").value("Hats"));

        verify(categoryService, times(1)).findAll();
    }

    // ========================== CREATE ============================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnCreated_whenAdmin() throws Exception {
        // Arrange
        when(categoryService.create(any(CategoryRequestDTO.class)))
                .thenReturn(new CategoryResponseDTO(1L, "Shoes"));

        // Act & Assert
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("Shoes"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Shoes"));

        verify(categoryService, times(1)).create(any(CategoryRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("Shoes"))))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).create(any(CategoryRequestDTO.class));
    }

    @Test
    void create_shouldReturnUnauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("Shoes"))))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).create(any(CategoryRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Category name is required."));

        verify(categoryService, never()).create(any(CategoryRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnBadRequest_whenNameIsTooLong() throws Exception {
        String longName = "A".repeat(101);

        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO(longName))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Category name can be at most 100 characters."));

        verify(categoryService, never()).create(any(CategoryRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnConflict_whenNameAlreadyExists() throws Exception {
        // Arrange
        String message = "Category with name: Shoes already exists.";
        when(categoryService.create(any(CategoryRequestDTO.class)))
                .thenThrow(new CategoryAlreadyExistsException(message));

        // Act & Assert
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("Shoes"))))
                .andExpect(status().isConflict())
                .andExpect(content().string(message));
    }
}
