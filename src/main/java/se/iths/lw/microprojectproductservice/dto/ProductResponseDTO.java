package se.iths.lw.microprojectproductservice.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;



public record ProductResponseDTO (

    String uuid,
    String name,
    BigDecimal price,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

}

