package se.iths.lw.microprojectproductservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.iths.lw.microprojectproductservice.dto.CategoryResponseDTO;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.exception.CategoryNotFoundException;
import se.iths.lw.microprojectproductservice.exception.ProductNotFoundException;
import se.iths.lw.microprojectproductservice.service.ProductService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//controller-layer tests for the category parts of ProductController:

@SpringBootTest
@AutoConfigureMockMvc
class ProductCategoryControllerLayerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private String sampleUuid;
    private ProductResponseDTO productWithCategory;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        productWithCategory = new ProductResponseDTO(
                1L,
                sampleUuid,
                "Sneaker",
                null,
                new BigDecimal("99.99"),
                "Description",
                10,
                new CategoryResponseDTO(1L, "Shoes"),
                now,
                now
        );
    }

    // ===================== GET /products/category/{categoryId} =====================

    @Test
    void getProductsByCategory_shouldReturnProducts_withoutLogin() throws Exception {
        // Arrange
        when(productService.findByCategoryId(1L)).thenReturn(List.of(productWithCategory));

        // Act & Assert
        mockMvc.perform(get("/products/category/{categoryId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Sneaker"))
                .andExpect(jsonPath("$[0].category.id").value(1))
                .andExpect(jsonPath("$[0].category.name").value("Shoes"));

        verify(productService, times(1)).findByCategoryId(1L);
    }

    @Test
    void getProductsByCategory_shouldReturnEmptyList_whenCategoryHasNoProducts() throws Exception {
        // Arrange
        when(productService.findByCategoryId(2L)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/products/category/{categoryId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getProductsByCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        // Arrange
        String message = "Category with id: 999 does not exist.";
        when(productService.findByCategoryId(999L)).thenThrow(new CategoryNotFoundException(message));

        // Act & Assert
        mockMvc.perform(get("/products/category/{categoryId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(message));
    }

    // ===================== PATCH /products/{uuid}/category =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldReturnUpdatedProduct_whenAdmin() throws Exception {
        // Arrange
        when(productService.updateCategory(sampleUuid, 1L)).thenReturn(productWithCategory);

        // Act & Assert
        mockMvc.perform(patch("/products/{uuid}/category", sampleUuid)
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(sampleUuid))
                .andExpect(jsonPath("$.category.name").value("Shoes"));

        verify(productService, times(1)).updateCategory(sampleUuid, 1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategory_shouldReturnForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(patch("/products/{uuid}/category", sampleUuid)
                        .param("categoryId", "1"))
                .andExpect(status().isForbidden());

        verify(productService, never()).updateCategory(anyString(), anyLong());
    }

    @Test
    void updateCategory_shouldReturnUnauthorized_whenNotLoggedIn() throws Exception {
        mockMvc.perform(patch("/products/{uuid}/category", sampleUuid)
                        .param("categoryId", "1"))
                .andExpect(status().isUnauthorized());

        verify(productService, never()).updateCategory(anyString(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        // Arrange
        when(productService.updateCategory(sampleUuid, 999L))
                .thenThrow(new CategoryNotFoundException("Category with id: 999 does not exist."));

        // Act & Assert
        mockMvc.perform(patch("/products/{uuid}/category", sampleUuid)
                        .param("categoryId", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        // Arrange
        when(productService.updateCategory(sampleUuid, 1L))
                .thenThrow(new ProductNotFoundException("Product with UUID: " + sampleUuid + " does not exist."));

        // Act & Assert
        mockMvc.perform(patch("/products/{uuid}/category", sampleUuid)
                        .param("categoryId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldReturnBadRequest_whenCategoryIdIsMissing() throws Exception {
        mockMvc.perform(patch("/products/{uuid}/category", sampleUuid))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateCategory(anyString(), anyLong());
    }

    // ===================== POST /products/new with categoryId =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldPassCategoryIdToService_whenAdmin() throws Exception {
        // Arrange
        ProductRequestDTO request = new ProductRequestDTO(
                "Sneaker",
                null,
                new BigDecimal("99.99"),
                "Description",
                10,
                1L
        );
        when(productService.create(any(ProductRequestDTO.class))).thenReturn(productWithCategory);

        // Act & Assert
        mockMvc.perform(post("/products/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category.name").value("Shoes"));

        ArgumentCaptor<ProductRequestDTO> captor = ArgumentCaptor.forClass(ProductRequestDTO.class);
        verify(productService).create(captor.capture());
        assertEquals(1L, captor.getValue().categoryId());
    }
}