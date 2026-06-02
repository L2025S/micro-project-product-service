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
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.model.Product;
import se.iths.lw.microprojectproductservice.repository.ProductRepository;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Use H2 database
@Transactional //Roll back after every test
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;


    @BeforeEach
    void setUp(){
        productRepository.deleteAll();
    }


    // ========================================= TEST 1: CREATE PRODUCT ========================================

    @Test
    @WithMockUser(roles ="ADMIN")
    void createProduct_Success() throws Exception {

        ProductRequestDTO request = new ProductRequestDTO(
                "Test Product",
                new BigDecimal("199.99"),
                "This is a test product.",
                50
        );

        mockMvc. perform(post("/products/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.uuid",notNullValue()));

        List<Product> savedProducts = productRepository.findAll();
        assert(savedProducts.size() ==1);
        assert(savedProducts.get(0).getName().equals("Test Product"));
        assert(savedProducts.get(0).getStock() == 50);

    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_UserRole_ShouldReturnForbidden() throws Exception {
        ProductRequestDTO request  = new ProductRequestDTO(
                "Test product",
                new BigDecimal("199.99"),
                        "This is a test product.",
                        50);

        mockMvc.perform(post("/products/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());


    }

    // ============================ TEST 2: List all the products (USER or ADMIN) ====================================

    @Test
    @WithMockUser( roles = "USER")
    void listAllProducts_Success() throws Exception{

        Product product1 = Product.create("Product A", "Description A", new BigDecimal("99.99"),10);
        Product product2 = Product.create("Product B", "Description B", new BigDecimal("149.99"),20);

        productRepository.saveAll(List.of(product1, product2));

        mockMvc.perform(get("/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$.[0].name").value("Product A"))
                .andExpect(jsonPath("$.[1].name").value("Product B"));
    }


    // ================================ TEST 3: find product by ID (USER or ADMIN) ==================================

    @Test
    @WithMockUser( roles = "USER")
    void getProductById_Success() throws Exception {
        Product product = Product.create("Product C",
                "Description C",
                new BigDecimal("299.99"),
                30);

        Product savedProduct = productRepository.save(product);

        mockMvc.perform(get("/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product C"))
                .andExpect(jsonPath("$.price").value("299.99"))
                .andExpect(jsonPath("$.stock").value(30));
    }

    @Test
    @WithMockUser( roles = "USER")
    void getProductById_NotFound_returns404()throws Exception{
        mockMvc.perform(get("/products/9999"))
                .andExpect(status().isNotFound());
    }


    // ============================================ TEST 4: get product by uuid (ADMIN or USER) ===========================

    @Test
    @WithMockUser( roles = "USER")
    void getProductByUuid_Success() throws Exception {
        Product product = Product.create(
                "UUID product",
                "Description",
                new BigDecimal("399.99"),
                40);

        Product savedProduct = productRepository.save(product);

        mockMvc.perform(get("/products/uuid/{uuid}", savedProduct.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UUID product"))
                .andExpect(jsonPath("$.uuid").value(savedProduct.getUuid()));
    }


}
