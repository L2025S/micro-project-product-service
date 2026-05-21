package se.iths.lw.microprojectproductservice.dto;


import java.math.BigDecimal;

public record ProductStockResponseDTO(
        Long productId,
        String name,
        BigDecimal price,
        int requestedQuantity,
        int remainStock,
        String status,
        String message
) {
}
