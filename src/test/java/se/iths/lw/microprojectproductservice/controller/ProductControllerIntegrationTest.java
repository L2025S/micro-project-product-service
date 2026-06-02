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
    void skapaProduct_Success() throws Exception {

        ProductRequestDTO request = new ProductRequestDTO(
                "Test Product",
                new BigDecimal("199.99"),
                "Detta är en testprodukt",
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


}
