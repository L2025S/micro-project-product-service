package se.iths.lw.microprojectproductservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.iths.lw.microprojectproductservice.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
