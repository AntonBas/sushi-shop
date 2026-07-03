package com.sushishop.mapper;

import com.sushishop.domain.User;
import com.sushishop.domain.enums.UserRole;
import com.sushishop.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldMapToResponse() {
        User user = User.builder()
                .id(1L)
                .name("Anton")
                .email("anton@example.com")
                .phone("+380961791111")
                .city("Lviv")
                .street("Zelena")
                .house("204")
                .apartment("280")
                .password("encoded")
                .userRole(UserRole.CUSTOMER)
                .build();

        UserResponse response = userMapper.toResponse(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Anton");
        assertThat(response.email()).isEqualTo("anton@example.com");
        assertThat(response.phone()).isEqualTo("+380961791111");
        assertThat(response.role()).isEqualTo("CUSTOMER");
        assertThat(response.address()).isNull();
    }
}