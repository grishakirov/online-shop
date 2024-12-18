package cz.cvut.fit.tjv.online_store.controller;

import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import cz.cvut.fit.tjv.online_store.domain.Role;
import cz.cvut.fit.tjv.online_store.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void shouldGetUserById() throws Exception {
        UserDto user = new UserDto(1L, "John", "Doe", "john.doe@example.com", "hashed_password", Role.CUSTOMER);
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.surname", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.password", is("hashed_password")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserDto savedUser = new UserDto(2L, "Jane", "Doe", "jane.doe@example.com", "secure_password", Role.ADMINISTRATOR);

        when(userService.save(Mockito.any(UserDto.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users/registr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "Jane",
                            "surname": "Doe",
                            "email": "jane.doe@example.com",
                            "password": "secure_password",
                            "role": "ADMINISTRATOR"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Jane")))
                .andExpect(jsonPath("$.surname", is("Doe")))
                .andExpect(jsonPath("$.email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$.password", is("secure_password")))
                .andExpect(jsonPath("$.role", is("ADMINISTRATOR")));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserDto updatedUser = new UserDto(1L, "Updated", "Doe", "updated.doe@example.com", "new_password", Role.CUSTOMER);

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\",\"surname\":\"Doe\",\"email\":\"updated.doe@example.com\",\"password\":\"new_password\",\"role\":\"CUSTOMER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.surname", is("Doe")))
                .andExpect(jsonPath("$.email", is("updated.doe@example.com")))
                .andExpect(jsonPath("$.password", is("new_password")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUserIfNoActiveOrders(1L);

        mockMvc.perform(delete("/users/1?with-check=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserIfNoActiveOrders(1L);
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        UserDto user1 = new UserDto(1L, "John", "Doe", "john.doe@example.com", "hashed_password", Role.CUSTOMER);
        UserDto user2 = new UserDto(2L, "Jane", "Doe", "jane.doe@example.com", "secure_password", Role.ADMINISTRATOR);
        when(userService.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[0].surname", is("Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[0].password", is("hashed_password")))
                .andExpect(jsonPath("$[0].role", is("CUSTOMER")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane")))
                .andExpect(jsonPath("$[1].surname", is("Doe")))
                .andExpect(jsonPath("$[1].email", is("jane.doe@example.com")))
                .andExpect(jsonPath("$[1].password", is("secure_password")))
                .andExpect(jsonPath("$[1].role", is("ADMINISTRATOR")));
    }
}