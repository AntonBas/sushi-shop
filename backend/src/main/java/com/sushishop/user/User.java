package com.sushishop.user;

import com.sushishop.shared.BaseEntity;
import com.sushishop.shared.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String street;

    @Column(length = 10)
    private String house;

    @Column(length = 10)
    private String apartment;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokenVersion = 0;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole userRole = UserRole.CUSTOMER;
}
