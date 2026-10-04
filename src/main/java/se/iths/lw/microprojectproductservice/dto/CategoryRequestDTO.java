package se.iths.lw.microprojectproductservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO(

        @NotBlank(message =  "Category name is required.")
        @Size(max = 100, message = "Category name can be at most 100 characters.")
        String name
) {
}
