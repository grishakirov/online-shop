package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Test resource not found")));
    }

    @Test
    void shouldHandleConflictException() throws Exception {
        mockMvc.perform(post("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Conflict occurred")));
    }

    @Test
    void shouldHandleIllegalStateException() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict()) // Updated to match the new 409 Conflict status for IllegalStateException
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Illegal state occurred")));
    }

    @Test
    void shouldHandleGeneralException() throws Exception {
        mockMvc.perform(get("/test/general-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred. Please try again later.")));
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @GetMapping("/illegal-argument")
        public void triggerIllegalArgumentException() {
            throw new IllegalArgumentException("Test resource not found");
        }

        @PostMapping("/conflict")
        public void triggerConflictException() {
            throw new ConflictException("Conflict occurred");
        }

        @GetMapping("/illegal-state")
        public void triggerIllegalStateException() {
            throw new IllegalStateException("Illegal state occurred");
        }

        @GetMapping("/general-exception")
        public void triggerGeneralException() {
            throw new RuntimeException("General exception occurred");
        }
    }
}