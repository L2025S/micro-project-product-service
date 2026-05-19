package se.iths.lw.microprojectproductservice.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target="uuid",ignore = true)
    @Mapping(target="createdAt", ignore = true)
    @Mapping(target="updateAt", ignore = true)
    Product toEntity(ProductRequestDTO productRequestDTO);

    ProductResponseDTO toResponseDTO(Product product);

}
