package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    void shouldHandleIllegalArgumentExceptionWithNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found-argument"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Resource not found")));
    }

    @Test
    void shouldHandleIllegalArgumentExceptionWithBadRequest() throws Exception {
        mockMvc.perform(get("/test/bad-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Invalid argument provided")));
    }

    @Test
    void shouldHandleHttpMessageNotReadableException() throws Exception {
        mockMvc.perform(post("/test/http-message-not-readable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invalid\":0}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());
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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Illegal state occurred")));
    }

    @Test
    void shouldHandleAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message").isNotEmpty());
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

        @GetMapping("/not-found-argument")
        public void triggerIllegalArgumentExceptionForNotFound() {
            throw new IllegalArgumentException("Resource not found");
        }

        @GetMapping("/bad-argument")
        public void triggerIllegalArgumentExceptionForBadRequest() {
            throw new IllegalArgumentException("Invalid argument provided");
        }

        @PostMapping("/http-message-not-readable")
        public void triggerHttpMessageNotReadableException() {
            throw new HttpMessageNotReadableException("Test exception: invalid JSON");
        }

        @PostMapping("/conflict")
        public void triggerConflictException() {
            throw new ConflictException("Conflict occurred");
        }

        @GetMapping("/illegal-state")
        public void triggerIllegalStateException() {
            throw new IllegalStateException("Illegal state occurred");
        }

        @GetMapping("/access-denied")
        public void triggerAccessDeniedException() {
            throw new org.springframework.security.access.AccessDeniedException("Access is denied");
        }

        @GetMapping("/general-exception")
        public void triggerGeneralException() {
            throw new RuntimeException("General exception occurred");
        }
    }
}