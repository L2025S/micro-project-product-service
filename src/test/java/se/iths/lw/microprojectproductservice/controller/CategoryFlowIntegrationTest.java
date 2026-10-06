package se.iths.lw.microprojectproductservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import se.iths.lw.microprojectproductservice.dto.CategoryRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.model.Category;
import se.iths.lw.microprojectproductservice.model.Product;
import se.iths.lw.microprojectproductservice.repository.CategoryRepository;
import se.iths.lw.microprojectproductservice.repository.ProductRepository;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//  end-to-end tests for the category feature with the real service, real repositories and the H2 database

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use H2 database
@Transactional // Roll back after every test
class CategoryFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Products first, because they have a foreign key to categories
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // ============================== CATEGORIES ==============================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_Success() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("Shoes"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Shoes")));

        assertEquals(1, categoryRepository.findAll().size());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_TrimsTheName() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("  Hats  "))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Hats")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_DuplicateNameWithDifferentCase_Returns409() throws Exception {
        categoryRepository.save(Category.create("Shoes"));

        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("shoes"))))
                .andExpect(status().isConflict());

        assertEquals(1, categoryRepository.findAll().size());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_UserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequestDTO("Shoes"))))
                .andExpect(status().isForbidden());

        assertEquals(0, categoryRepository.findAll().size());
    }

    @Test
    void listCategories_Success_withoutLogin() throws Exception {
        categoryRepository.save(Category.create("Shoes"));
        categoryRepository.save(Category.create("Hats"));

        mockMvc.perform(get("/categories/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Shoes", "Hats")));
    }

    // ============================== PRODUCTS WITH CATEGORY ==============================

    @Test
    void getAllProducts_IncludesTheCategory() throws Exception {
        Category shoes = categoryRepository.save(Category.create("Shoes"));
        productRepository.save(Product.create(
                "Sneaker", null, "Description", new BigDecimal("99.99"), 10, shoes));

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category.id", is(shoes.getId().intValue())))
                .andExpect(jsonPath("$[0].category.name", is("Shoes")));
    }

    @Test
    void getAllProducts_ProductWithoutCategory_HasNullCategory() throws Exception {
        productRepository.save(Product.create(
                "Old product", null, "Description", new BigDecimal("49.99"), 5, null));

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", nullValue()));
    }

    @Test
    void getProductsByCategory_ReturnsOnlyProductsOfThatCategory() throws Exception {
        Category shoes = categoryRepository.save(Category.create("Shoes"));
        Category hats = categoryRepository.save(Category.create("Hats"));
        productRepository.save(Product.create(
                "Sneaker", null, "Description", new BigDecimal("99.99"), 10, shoes));
        productRepository.save(Product.create(
                "Cap", null, "Description", new BigDecimal("19.99"), 20, hats));

        mockMvc.perform(get("/products/category/{categoryId}", shoes.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Sneaker")))
                .andExpect(jsonPath("$[0].category.name", is("Shoes")));
    }

    @Test
    void getProductsByCategory_CategoryWithoutProducts_ReturnsEmptyList() throws Exception {
        Category hats = categoryRepository.save(Category.create("Hats"));

        mockMvc.perform(get("/products/category/{categoryId}", hats.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getProductsByCategory_UnknownCategory_Returns404() throws Exception {
        mockMvc.perform(get("/products/category/{categoryId}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_Success() throws Exception {
        Category shoes = categoryRepository.save(Category.create("Shoes"));
        Product product = productRepository.save(Product.create(
                "Sneaker", null, "Description", new BigDecimal("99.99"), 10, null));

        mockMvc.perform(patch("/products/{uuid}/category", product.getUuid())
                        .param("categoryId", String.valueOf(shoes.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category.name", is("Shoes")));

        Product updated = productRepository.findByUuid(product.getUuid()).orElseThrow();
        assertEquals("Shoes", updated.getCategory().getName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_UnknownCategory_Returns404() throws Exception {
        Product product = productRepository.save(Product.create(
                "Sneaker", null, "Description", new BigDecimal("99.99"), 10, null));

        mockMvc.perform(patch("/products/{uuid}/category", product.getUuid())
                        .param("categoryId", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategory_UserRole_ShouldReturnForbidden() throws Exception {
        Category shoes = categoryRepository.save(Category.create("Shoes"));
        Product product = productRepository.save(Product.create(
                "Sneaker", null, "Description", new BigDecimal("99.99"), 10, null));

        mockMvc.perform(patch("/products/{uuid}/category", product.getUuid())
                        .param("categoryId", String.valueOf(shoes.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithCategoryId_Success() throws Exception {
        Category shoes = categoryRepository.save(Category.create("Shoes"));
        ProductRequestDTO request = new ProductRequestDTO(
                "Sneaker", null, new BigDecimal("99.99"), "Description", 10, shoes.getId());

        mockMvc.perform(post("/products/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category.name", is("Shoes")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithUnknownCategoryId_Returns404() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO(
                "Sneaker", null, new BigDecimal("99.99"), "Description", 10, 999999L);

        mockMvc.perform(post("/products/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertEquals(0, productRepository.findAll().size());
    }
}