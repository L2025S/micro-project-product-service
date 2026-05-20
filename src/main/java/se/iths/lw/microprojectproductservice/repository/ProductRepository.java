package se.iths.lw.microprojectproductservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.iths.lw.microprojectproductservice.model.Product;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product>findById(Long id);
    boolean existsById(Long id);
    Optional<Product>findByUuid(String uuid);
}
