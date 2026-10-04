package se.iths.lw.microprojectproductservice.mapper;


import org.mapstruct.Mapper;
import se.iths.lw.microprojectproductservice.dto.CategoryResponseDTO;
import se.iths.lw.microprojectproductservice.model.Category;

@Mapper(componentModel ="spring")
public interface CategoryMapper {

    // MapStruct mapper Category -> CategoryResponseDTO
    CategoryResponseDTO toResponseDTO (Category category);
}
