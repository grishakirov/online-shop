package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Test resource not found"));
    }

    @Test
    void shouldHandleConflictException() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Test resource conflict"));
    }

    @Test
    void shouldHandleIllegalStateException() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Illegal state occurred")));
    }

    @Test
    void shouldHandleGeneralException() throws Exception {
        mockMvc.perform(get("/test/general-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: General exception occurred"));
    }

    @RestController
    private static class TestController {
        @GetMapping("/test/illegal-argument")
        public ResponseEntity<String> illegalArgument() {
            throw new IllegalArgumentException("Test resource");
        }

        @GetMapping("/test/conflict")
        public ResponseEntity<String> conflict() {
            throw new ConflictException("Test resource");
        }

        @GetMapping("/test/illegal-state")
        public ResponseEntity<String> illegalState() {
            throw new IllegalStateException("Illegal state occurred");
        }

        @GetMapping("/test/general-exception")
        public ResponseEntity<String> generalException() {
            throw new RuntimeException("General exception occurred");
        }
    }
}