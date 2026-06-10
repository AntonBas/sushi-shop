package com.sushishop.domain;

import com.sushishop.domain.enums.UserRole;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    private Long id;
    private String email;

    private String firstName;
    private String lastName;

    private Long phoneNumber;
    private String password;

    private UserRole userRole = UserRole.ROLE_USER;
}
