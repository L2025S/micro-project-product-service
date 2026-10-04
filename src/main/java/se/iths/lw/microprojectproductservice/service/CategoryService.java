package se.iths.lw.microprojectproductservice.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.iths.lw.microprojectproductservice.dto.CategoryRequestDTO;
import se.iths.lw.microprojectproductservice.dto.CategoryResponseDTO;
import se.iths.lw.microprojectproductservice.exception.CategoryAlreadyExistsException;
import se.iths.lw.microprojectproductservice.mapper.CategoryMapper;
import se.iths.lw.microprojectproductservice.model.Category;
import se.iths.lw.microprojectproductservice.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ================================= Create =====================================


    public CategoryResponseDTO create (CategoryRequestDTO categoryRequestDTO) {
        String name = categoryRequestDTO.name().trim();

        if( categoryRepository.existsByNameIgnoreCase(name)) {
            throw new CategoryAlreadyExistsException("Category with name: " + name + " already exists.");
        }
        Category category = Category. create(name);

        return categoryMapper.toResponseDTO(categoryRepository.save(category));
    }


    // =================================== Read =======================================

    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper:: toResponseDTO)
                .collect(Collectors.toList());
    }
}
