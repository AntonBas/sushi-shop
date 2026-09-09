package com.sushishop.user;

import com.sushishop.shared.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank
    @Email
    @Size(max = 100)
    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 15)
    @Column(length = 15)
    private String phone;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String street;

    @Column(length = 10)
    private String house;

    @Column(length = 10)
    private String apartment;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokenVersion = 0;

    @Size(min = 8, max = 100)
    @Column(length = 100)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole userRole = UserRole.CUSTOMER;
}
