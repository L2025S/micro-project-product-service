package se.iths.lw.microprojectproductservice.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.exception.ProductNotFoundException;
import se.iths.lw.microprojectproductservice.service.ProductService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hamcrest.Matchers.*;



@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductResponseDTO sampleProductResponse;
    private ProductRequestDTO sampleProductRequest;
    private String sampleUuid;
    private LocalDateTime now;

    @BeforeEach
    void setUp(){
        sampleUuid = UUID.randomUUID().toString();
        now = LocalDateTime.now();

        sampleProductResponse = new ProductResponseDTO(
                sampleUuid,
                "Test Product",
                new BigDecimal("99.99"),
                "Test Description",
                100,
                now,
                null
        );

        sampleProductRequest = new ProductRequestDTO(
                "Test Product",
                new BigDecimal("99.99"),
                "Test Description",
                100
        );
    }


    //============================== READ TESTS ==========================================

    @Test
    @WithMockUser(roles = "USER")
    void getProductById_ShouldReturnProduct_WhenProductExists() throws Exception {

        // Arrange
        when(productService.findById(1L)).thenReturn(sampleProductResponse);

        mockMvc.perform(get("/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(sampleUuid))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.stock").value(100))
                .andExpect(jsonPath("$.createdAt").value(notNullValue()));

        verify(productService, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductById_ShouldThrow_WhenProductDoesNotExist() throws Exception {

        //Arrange
        Long nonExistentId = 999L;
        String errorMessage = "Product with ID: 999 does not exist.";
        when(productService.findById(nonExistentId)).thenThrow(new ProductNotFoundException(errorMessage));

        //Act & Assert

        mockMvc.perform(get("/products/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                        .andExpect(content().string(errorMessage));


        verify(productService, times(1)).findById(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductByUuid_ShouldReturnProduct_WhenProductExists() throws Exception {
        //Arrange
        when (productService.findByUuid(sampleUuid)).thenReturn(sampleProductResponse);

        //Act & Assert

        mockMvc.perform(get("/products/uuid/{uuid}", sampleUuid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(sampleUuid))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).findByUuid(sampleUuid);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProductByUuid_ShouldThrow_WhenNotExists() throws Exception{

        // Arrange
        String nonExistentUuid = UUID.randomUUID().toString();
        when(productService.findByUuid(nonExistentUuid)).thenThrow(new ProductNotFoundException (
                "Product with ID: " + nonExistentUuid + " does not exist."));

        // Act & Assert
        mockMvc.perform(get("/products/uuid/{uuid}", nonExistentUuid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).findByUuid(nonExistentUuid);

    }



    @Test
    @WithMockUser(roles = "USER")
    void getAllProducts_ShouldReturnListOfProducts() throws Exception {

        // Arrange
        ProductResponseDTO productResponse2 = new ProductResponseDTO(
                UUID.randomUUID().toString(),
                "Product 2",
                new BigDecimal("49.99"),
                "Description 2",
                50,
                now,
                null
        );

        List<ProductResponseDTO> responseDTOS = List.of(sampleProductResponse, productResponse2);
        when(productService.findAll()).thenReturn(responseDTOS);

        // Act & Assert
        mockMvc.perform(get("/products/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[1].price").value(49.99));

        verify(productService, times(1)).findAll();

    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllProductsPaged_ShouldReturnPageOfProducts() throws Exception {

        //Arrange
        Page<ProductResponseDTO> pagedResponse = new PageImpl<>(List.of(sampleProductResponse));

        when(productService.findAll(any(Pageable.class))).thenReturn(pagedResponse);

        //Act & Assert
        mockMvc.perform(get("/products/all-paged")
                .param("page","0")
                .param("size","10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.content[0].uuid").value(sampleUuid));

        verify(productService, times(1)).findAll(any(Pageable.class));
    }



    // ================================== CREATE TESTS =================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnCreated_WhenValidRequests() throws Exception {
        // Arrange
        
        when(productService.create(any(ProductRequestDTO.class))).thenReturn(sampleProductResponse);

        // Act & Assert
        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.stock").value(100));

        verify(productService, times(1)).create(any(ProductRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_ShouldReturnForbidden_WhenUserDoesNotHaveAdminRole() throws Exception {

        // Arrange
        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProductRequest)))
                .andExpect(status().isForbidden());

        verify(productService, never()).create(any(ProductRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnBadRequest_whenNameIsBlank() throws Exception {

        //Arrange
        ProductRequestDTO invalidRequest = new ProductRequestDTO(
                "",
                new BigDecimal("99.99"),
                "Description",
                10
        );

        // Act & Assert

        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).create(any(ProductRequestDTO.class));
    }

    @Test
    @WithMockUser( roles = "ADMIN")
    void create_ShouldReturnBadRequest_WhenPriceIsZeroOrNegative() throws Exception {
        // Arrange

        ProductRequestDTO invalidRequest = new ProductRequestDTO(
                "Test Product",
                new BigDecimal("0"),
                "Description",
                10
        );

        // Act & Assert
        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).create(any(ProductRequestDTO.class));
    }

    @Test
    @WithMockUser( roles = "ADMIN")
    void create_ShouldReturnBadRequest_WhenStockIsNegative() throws Exception {

        // Arrange
        ProductRequestDTO invalidRequest = new ProductRequestDTO(
                "Test Product",
                new BigDecimal("99.99"),
                "Description",
                -5
        );

        // Act & Assert

        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).create(any(ProductRequestDTO.class));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void create_ShouldReturnBadRequest_WhenNameExceedsMaxLength() throws Exception {
        String longName = "A".repeat(401);
        ProductRequestDTO invalidRequest = new ProductRequestDTO(
                longName,
                new BigDecimal("99.99"),
                "Description",
                10
        );

        // Act & Assert
        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).create(any(ProductRequestDTO.class));
    }









}
