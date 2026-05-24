package se.iths.lw.microprojectproductservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.dto.ProductStockRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductStockResponseDTO;
import se.iths.lw.microprojectproductservice.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;





 // ============================ READ =============================================
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(productService.findById(id));
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<ProductResponseDTO> getProductByUuid(@PathVariable String uuid){
        return ResponseEntity.status(HttpStatus.OK).body(productService.findByUuid(uuid));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(){
        return ResponseEntity.status(HttpStatus.OK).body(productService.findAll());
    }

    @GetMapping("/all-paged")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProductsPaged(Pageable pageable){


        Page<ProductResponseDTO> products = productService.findAll(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    // ========================== CREATE ============================================
    @PostMapping("/new")
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO productRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productRequestDTO));

    }

    //============================== UPDATE ===========================================
    @PatchMapping("/{uuid}/stock/reduce")
    public ResponseEntity<ProductResponseDTO> reduceStock(
            @PathVariable String uuid,
            @RequestParam int quantity){

        return ResponseEntity.status(HttpStatus.OK).body(productService.reduceStock(uuid, quantity));
    }

    @PatchMapping("/{uuid}/stock/increase")
    public ResponseEntity<ProductResponseDTO> increaseStock(
            @PathVariable String uuid,
            @RequestParam int quantity) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.increaseStock(uuid, quantity));
    }

    @PatchMapping("/{uuid}/basic-info")
    public ResponseEntity<ProductResponseDTO> updateBasicInfo(
            @PathVariable String uuid,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price){

        return ResponseEntity.status(HttpStatus.OK).body(productService.updateBasicInfo(uuid, name, description, price));

    }


    //=================================== Delete ===============================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        productService.deleteById(id);
        return ResponseEntity.noContent().build();

    }

    @DeleteMapping("/uuid/{uuid}")
    public ResponseEntity<Void> deleteByUuid(@PathVariable String uuid){
        productService.deleteByUuid(uuid);
        return ResponseEntity.noContent().build();
    }

    //========================== The method below is for order-service ===================

    @PostMapping("/stock/decrease")
    public ResponseEntity<List<ProductStockResponseDTO>> decreaseStockBatch(
            @Valid @RequestBody List<ProductStockRequestDTO> requests){
        List<ProductStockResponseDTO> responses = productService.decreaseStockBatch(requests);

        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }


}
