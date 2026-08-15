package com.sushishop.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class FileControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FileStorageService fileStorageService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldReturnFile() throws Exception {
        var resource = new UrlResource(Path.of("build.gradle").toUri());

        when(fileStorageService.load("test.jpg")).thenReturn(resource);
        when(fileStorageService.determineContentType("test.jpg")).thenReturn("image/jpeg");

        mockMvc.perform(get("/api/files/test.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn404ForMissingFile() throws Exception {
        when(fileStorageService.load("missing.jpg")).thenReturn(null);

        mockMvc.perform(get("/api/files/missing.jpg"))
                .andExpect(status().isNotFound());
    }
}