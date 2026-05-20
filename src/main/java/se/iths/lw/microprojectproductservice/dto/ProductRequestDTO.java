package se.iths.lw.microprojectproductservice.dto;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank(message= "Name is required.")
    @Size(max = 400, message="Product name can be at most 400 characters.")
    private String name;

    @NotBlank
    @DecimalMin(value = "0.01", message="Price must be > 0.")
    private BigDecimal price;

    @Size(max = 1000, message="Description can have a maximum of 1000 characters. ")
    private String description;

    @PositiveOrZero
    private int stock;
}
