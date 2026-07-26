package com.sushishop.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class FileControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        var field = FileController.class.getDeclaredField("uploadDir");
        field.setAccessible(true);
        field.set(context.getBean(FileController.class), tempDir.toString());
        Files.writeString(tempDir.resolve("test.jpg"), "fake-image");
    }

    @Test
    void shouldReturnFile() throws Exception {
        mockMvc.perform(get("/api/files/test.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404ForMissingFile() throws Exception {
        mockMvc.perform(get("/api/files/missing.jpg"))
                .andExpect(status().isNotFound());
    }
}