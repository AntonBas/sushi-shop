package com.sushishop.user;

import com.sushishop.shared.enums.UserRole;
import com.sushishop.user.dto.response.UserResponse;
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
        assertThat(response.userRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.address()).isNotNull();
        assertThat(response.address().city()).isEqualTo("Lviv");
        assertThat(response.address().street()).isEqualTo("Zelena");
        assertThat(response.address().house()).isEqualTo("204");
        assertThat(response.address().apartment()).isEqualTo("280");
    }
}