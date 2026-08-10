package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.shared.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "promotions")
@EqualsAndHashCode(callSuper = true, exclude = "products")
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 250)
    private String description;

    @NotNull
    @Digits(integer = 3, fraction = 2)
    @DecimalMin("0.01")
    @DecimalMax("90.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Future
    @Column(nullable = false)
    private LocalDateTime endDate;

    @ManyToMany
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "product_id"})
    )
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
