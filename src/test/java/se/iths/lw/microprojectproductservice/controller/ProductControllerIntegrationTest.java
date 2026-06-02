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
import se.iths.lw.microprojectproductservice.dto.ProductStockRequestDTO;
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


    // ====================================== TEST 5: Delete product (ADMIN) ==================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductById_Success() throws Exception {
        Product product = Product.create(
                "Product D",
                "will be deleted",
                new BigDecimal("49.99"),
                5
        );

        Product saved = productRepository.save(product);

        mockMvc.perform(delete("/products/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assert(productRepository.findById(saved.getId()).isEmpty());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductByUuid_Success() throws Exception {
        Product product = Product.create(
                "Product UUID",
                "will be deleted by uuid",
                new BigDecimal("59.99"),
                8
        );

        Product saved = productRepository.save(product);

        mockMvc.perform(delete("/products/uuid/{uuid}",saved.getUuid()))
                .andExpect(status().isNoContent());

        assert(productRepository.findByUuid(saved.getUuid()).isEmpty());
    }


    @Test
    @WithMockUser( roles = "USER")
    void deleteProduct_UserRole_ShouldReturnForbidden() throws Exception {
        Product product = Product.create(
                "Product E",
                "will be deleted",
                new BigDecimal("49.99"),
                5
        );

        Product saved = productRepository.save(product);

        mockMvc.perform(delete("/products/{id}", saved.getId()))
                .andExpect(status().isForbidden());
    }



    // ================================= TEST 6: Reduce the stock (ADMIN or USER) =======================================

    @Test
    @WithMockUser( roles = "USER")
    void reduceStock_Success() throws Exception {
        Product product = Product.create(
                "Product F",
                "to reduce the stock",
                new BigDecimal("79.99"),
                100
        );

        Product saved = productRepository.save(product);

        mockMvc.perform(patch("/products/{uuid}/stock/reduce", saved.getUuid())
                .param("quantity","25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(75));


        Product updatedProduct = productRepository.findById(saved.getId()).get();
        assert(updatedProduct.getStock() == 75);
    }


    // ================================ TEST 7 : Return 400 when the stock is insufficient ============================

    @Test
    @WithMockUser( roles = "USER")
    void reduceStock_InsufficientStock_Returns400() throws Exception {
        Product product = Product.create(
                "Product G",
                "Description G",
                new BigDecimal("199.99"),
                5
        );
        Product saved = productRepository.save(product);

        mockMvc.perform(patch("/products/{uuid}/stock/reduce", saved.getUuid())
                .param("quantity", "10"))
                .andExpect(status().isBadRequest());

        Product unchangedProduct = productRepository.findById(saved.getId()).get();

        assert(unchangedProduct.getStock() == 5);
    }


    @Test
    @WithMockUser(roles = "USER")
    void reduceStock_NegativeQuantity_Returns400() throws Exception {
        Product product = Product.create(
                "Test Product",
                "Negative quantity will fail",
                new BigDecimal("99.99"),
                100
        );

        Product saved = productRepository.save(product);

        mockMvc.perform(patch("/products/{uuid}/stock/reduce", saved.getUuid())
                .param("quantity", "-5"))
                .andExpect(status().isBadRequest());
    }


    // ================================ TEST 8: increase stock (ADMIN or USER)  ===============================

    @Test
    @WithMockUser( roles = "USER")
    void increaseStock_Success() throws Exception {
        Product product = Product.create(
                "Product H",
                "Increase-stock test",
                new BigDecimal("49.99"),
                10
                );

        Product saved = productRepository.save(product);

        mockMvc.perform(patch("/products/{uuid}/stock/increase", saved.getUuid())
                .param("quantity", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(25));

        Product updatedProduct = productRepository.findById(saved.getId()).get();

        assert(updatedProduct.getStock() == 25);
    }



    // ============================= TEST 9: find all (USER or Admin) ============================================

    @Test
    @WithMockUser(roles = "USER")
    void getAllProducts_Pagination_Success() throws Exception {
        for(int i = 1; i <= 10; i++){
            Product product = Product.create(
                    "Product " + i,
                    "Description",
                    new BigDecimal("100.00"),
                    i*10
            );
            productRepository.save(product);
        }

        mockMvc.perform(get("/products/all-paged")
                .param("page","0")
                .param("size","5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is (10)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }


    // ================== TEST 10 : batch stock decrease ( for order-service, ADMIN/USER) ==========================

    @Test
    @WithMockUser(roles = "USER")
    void batchDecreaseStock_Success() throws Exception {
        Product product1 = Product.create(
                "Batch Product 1",
                "Description",
                new BigDecimal("100.00"),
                50);

        Product product2 = Product.create(
                "Batch Product 2",
                "Description",
                new BigDecimal("200.00"),
                30);

        productRepository.saveAll(List.of(product1, product2));

        List<ProductStockRequestDTO> requests = List.of(
                new ProductStockRequestDTO(product1.getId(), 10),
                new ProductStockRequestDTO(product2.getId(), 5)
        );

        mockMvc.perform(post("/products/stock/decrease")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("SUCCESS")))
                .andExpect(jsonPath("$[0].remainingStock", is(40)))
                .andExpect(jsonPath("$[1].status", is("SUCCESS")))
                .andExpect(jsonPath("$[1].remainingStock", is(25)));

    }


    @Test
    @WithMockUser( roles = "USER")
    void batchDecreaseStock_PartialFailure_ReturnsMixedStatuses() throws Exception {

        Product product1 = Product.create(
                "Product-low stock",
                "Description",
                new BigDecimal("100.00"),
                50
        );

        Product product2 = Product.create("Product - not enough stock",
                "Description",
                new BigDecimal("200.00"),
                2);

        productRepository.saveAll(List.of(product1, product2));

        List<ProductStockRequestDTO> requests = List.of(
                new ProductStockRequestDTO(product1.getId(), 10),
                new ProductStockRequestDTO(product2.getId(), 5)
        );

        mockMvc.perform(post("/products/stock/decrease")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("SUCCESS")))
                .andExpect(jsonPath("$[0].remainingStock", is(40)))
                .andExpect(jsonPath("$[1].status", is("FAILED")))
                .andExpect(jsonPath("$[1].message",notNullValue()));

    Product unchangedProduct = productRepository.findById(product2.getId()).get();
    assert(unchangedProduct.getStock() == 2);


    }







}
