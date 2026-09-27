package com.sushishop.shared.exception.api;

import com.sushishop.shared.exception.core.InternalServerException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ApiErrorHandlerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private MockMvc standaloneMockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        standaloneMockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new ApiErrorHandler())
                .build();
    }

    @Test
    void shouldReturnConflictWithoutSqlDetailsOnDatabaseConstraintViolation() throws Exception {
        standaloneMockMvc.perform(get("/constraint"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Database constraint violation"))
                .andExpect(jsonPath("$.debugMessage").doesNotExist());
    }

    @Test
    void shouldReturnInternalServerErrorOnOtherDataIntegrityViolation() throws Exception {
        standaloneMockMvc.perform(get("/data-integrity"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Database error"));
    }

    @Test
    void shouldReturnConflictOnOptimisticLockingFailure() throws Exception {
        standaloneMockMvc.perform(get("/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The resource was modified concurrently, please retry"));
    }

    @Test
    void shouldReturnBadRequestOnBeanValidationFailureAtCommit() throws Exception {
        standaloneMockMvc.perform(get("/commit-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].field").value("name"));
    }

    @Test
    void shouldNotExposeCauseMessageForServerErrors() throws Exception {
        standaloneMockMvc.perform(get("/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Payment session creation failed"))
                .andExpect(jsonPath("$.debugMessage").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestOnUnknownSortProperty() throws Exception {
        mockMvc.perform(get("/api/products").param("sort", "foo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown property 'foo'"));
    }

    record Named(@NotBlank String name) {
    }

    @RestController
    static class FailingController {

        private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        @GetMapping("/constraint")
        public void constraint() {
            throw new DataIntegrityViolationException("could not execute statement",
                    new org.hibernate.exception.ConstraintViolationException(
                            "duplicate key", new SQLException("duplicate key value violates unique constraint"),
                            "uk_users_email"));
        }

        @GetMapping("/optimistic-lock")
        public void optimisticLock() {
            throw new ObjectOptimisticLockingFailureException("Order", 1L);
        }

        @GetMapping("/data-integrity")
        public void dataIntegrity() {
            throw new DataIntegrityViolationException("value too long");
        }

        @GetMapping("/commit-validation")
        public void commitValidation() {
            throw new TransactionSystemException("Could not commit JPA transaction",
                    new ConstraintViolationException(validator.validate(new Named(""))));
        }

        @GetMapping("/internal")
        public void internal() {
            throw new InternalServerException("Payment session creation failed",
                    new IllegalStateException("stripe secret details"));
        }
    }
}
