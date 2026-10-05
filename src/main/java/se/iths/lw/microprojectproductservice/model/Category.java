package se.iths.lw.microprojectproductservice.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.iths.lw.microprojectproductservice.exception.InvalidParameterException;


@Getter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@Entity
@Table(name="categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable= false, length = 100)
    private String name;

    public static Category create( String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidParameterException("Category name can not be null or blank.");
        }
        Category category = new Category();
        category.name = name.trim();
        return category;
    }
}
