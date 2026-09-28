package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class Promotion extends BaseEntity {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

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

    public BigDecimal applyDiscount(BigDecimal price) {
        var remainingShare = ONE_HUNDRED.subtract(discountPercent);
        return price.multiply(remainingShare).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public boolean isCurrentlyActive() {
        var now = LocalDateTime.now();
        return active
                && startDate != null
                && endDate != null
                && startDate.isBefore(now)
                && endDate.isAfter(now);
    }
}
