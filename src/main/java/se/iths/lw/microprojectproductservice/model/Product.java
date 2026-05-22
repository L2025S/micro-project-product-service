package se.iths.lw.microprojectproductservice.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import se.iths.lw.microprojectproductservice.exception.InsufficientStockException;
import se.iths.lw.microprojectproductservice.exception.InvalidParameterException;
import se.iths.lw.microprojectproductservice.exception.InvalidStockException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@ToString(exclude={"description"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PROTECTED)
@Entity
@Table(name="products",
uniqueConstraints =@UniqueConstraint(columnNames = "uuid"), indexes = @Index(columnList ="name"))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique= true, updatable = false, nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @Column(nullable = false, length = 400)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product that = (Product) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(uuid);
    }

    public static Product create(String name, String description, BigDecimal price, int stock ) {

        if(price == null || price.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidParameterException("Price must be positive.");
        }
        if (stock < 0) {
            throw new InvalidStockException("Stock cannot be negative.");
        }
        Product product  = new Product();
        product.name = name;
        product.description = description;
        product.price = price;
        product.stock = stock;
        return product;
    }

    public void updateBasicInfo(String name, String description, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidParameterException("Price must be positive.");
        }
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidStockException("Stock cannot be negative.");
        }
        if(this.stock < quantity) {
            throw new InsufficientStockException("Insufficient stock. Current: " + this.stock
                    + ", requested " + quantity);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0){
            throw new InvalidStockException("Stock cannot be negative.");
        }

        this.stock += quantity;
    }


}
