package se.iths.lw.microprojectproductservice.dto;


import jakarta.validation.constraints.*;


import java.math.BigDecimal;

public record ProductRequestDTO (

    @NotBlank(message= "Name is required.")
    @Size(max = 400, message="Product name can be at most 400 characters.")
    String name,

    @NotNull (message = "Price cannot be null.")
    @DecimalMin(value = "0.01", message="Price must be > 0.")
    BigDecimal price,

    @Size(max = 1000, message="Description can have a maximum of 1000 characters. ")
    String description,

    @PositiveOrZero
    int stock

    ){
            }