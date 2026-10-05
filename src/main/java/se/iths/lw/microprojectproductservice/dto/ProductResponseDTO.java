package se.iths.lw.microprojectproductservice.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;



public record ProductResponseDTO (

    Long id,
    String uuid,
    String name,
    String imageUrl,
    BigDecimal price,
    String description,
    int stock,
    CategoryResponseDTO category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

}

