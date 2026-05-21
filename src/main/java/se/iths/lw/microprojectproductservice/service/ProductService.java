package se.iths.lw.microprojectproductservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.iths.lw.microprojectproductservice.dto.ProductRequestDTO;
import se.iths.lw.microprojectproductservice.dto.ProductResponseDTO;
import se.iths.lw.microprojectproductservice.exception.InvalidParameterException;
import se.iths.lw.microprojectproductservice.exception.ProductNotFoundException;
import se.iths.lw.microprojectproductservice.mapper.ProductMapper;
import se.iths.lw.microprojectproductservice.model.Product;
import se.iths.lw.microprojectproductservice.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Transactional
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    // ======================================= Create =======================================================
    public ProductResponseDTO create(ProductRequestDTO productRequestDTO){

       Product product = Product.create(
               productRequestDTO.name(),
               productRequestDTO.description(),
               productRequestDTO.price(),
               productRequestDTO.stock()
       );
       return productMapper.toResponseDTO(productRepository.save(product));
    }

    // ============================================== Read ================================================
    public ProductResponseDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()->new ProductNotFoundException("Product with id: " + id + " does not exist"));

        return productMapper.toResponseDTO(product);
    }


    public List<ProductResponseDTO> findAll(){
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


    public ProductResponseDTO findByUuid(String uuid) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(()-> new ProductNotFoundException("Product with id: " + uuid + " does not exist."));

        return productMapper.toResponseDTO(product);
    }

    public Page<ProductResponseDTO> findAll(Pageable pageable){
        return productRepository.findAll(pageable)
                .map(productMapper::toResponseDTO);
    }

    //============================================= Update ==================================================

    public ProductResponseDTO reduceStock (String uuid, int quantity) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(()->new ProductNotFoundException("Product with UUID: " + uuid + " does not exist."));
        product.reduceStock(quantity);

        Product saved = productRepository.save(product);

        return productMapper.toResponseDTO(saved);
    }

    public ProductResponseDTO increaseStock(String uuid, int quantity) {
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(()-> new ProductNotFoundException("Product with UUID: " + uuid + " does not exist."));

        product.increaseStock(quantity);
        Product saved = productRepository.save(product);

        return productMapper.toResponseDTO(saved);
    }


    public ProductResponseDTO updateBasicInfo(String uuid, String name, String description, BigDecimal price) {

        // Step 1. Validate the parameters
        if (uuid == null || uuid.isBlank()){
            throw new InvalidParameterException("Product UUID can not be null or blank.");
        }
        if (name == null || name.isBlank()){
            throw new InvalidParameterException("Product name can not be null or blank.");
        }
        if (price == null) {
            throw new InvalidParameterException("Price can not be null.");
        }
        if(price.compareTo(BigDecimal.ZERO)<=0){
            throw new InvalidParameterException("Price can not be negative.");
        }

        // Step 2. find the product
        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(()-> new ProductNotFoundException("Product with UUID: " + uuid + " does not exist."));

        product.updateBasicInfo( name, description,price);

        Product saved = productRepository.save(product);

        return productMapper.toResponseDTO(saved);

    }

    // ============================================= Delete ====================================================

    public void deleteById(Long id){

        if (id == null || id <= 0) {
            throw new InvalidParameterException("Invalid product ID.");
        }

        if(!productRepository.existsById(id)){
            throw new ProductNotFoundException("Product with ID: " + id +  "does not exist.");
        }

        productRepository.deleteById(id);

    }

    public void deleteByUuid(String uuid) {
        if(uuid == null || uuid.isBlank()){
            throw new InvalidParameterException("Product UUID can not be null or blank.");
        }

        Product product = productRepository.findByUuid(uuid)
                .orElseThrow(()-> new ProductNotFoundException("Product with UUID: " + uuid + " does not exist."));

        productRepository.delete(product);
    }


    //============================= The method below is for order-service ==============================




}
