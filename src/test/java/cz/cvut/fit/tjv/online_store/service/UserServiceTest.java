package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void shouldCreateUserSuccessfully() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        UserDto userDto = new UserDto(1L, "John", "Doe", "john.doe@example.com", "123456789", null);

        when(userMapper.convertToEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.convertToDto(user)).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getSurname(), result.getSurname());

        verify(userMapper).convertToEntity(userDto);
        verify(userRepository).save(user);
        verify(userMapper).convertToDto(user);
    }

    @Test
    void shouldReturnUserWhenIdExists() {
        User user = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        UserDto userDto = new UserDto(1L, "John", "Doe", "john.doe@example.com", "123456789", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.convertToDto(user)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("Doe", result.getSurname());

        verify(userRepository).findById(1L);
        verify(userMapper).convertToDto(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.findById(1L));
        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(1L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UserDto updatedUserDto = new UserDto(1L, "UpdatedName", "UpdatedSurname", "updated.email@example.com", "123456789", null);
        User updatedUser = new User(1L, "UpdatedName", "UpdatedSurname", "updated.email@example.com", null, LocalDate.of(1999, 11, 11));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userMapper.convertToEntity(updatedUserDto)).thenReturn(updatedUser);
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(userMapper.convertToDto(updatedUser)).thenReturn(updatedUserDto);

        UserDto result = userService.update(1L, updatedUserDto);

        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        assertEquals("UpdatedSurname", result.getSurname());

        verify(userRepository).existsById(1L);
        verify(userMapper).convertToEntity(updatedUserDto);
        verify(userRepository).save(updatedUser);
        verify(userMapper).convertToDto(updatedUser);
    }

    @Test
    void shouldDeleteUserIfNoActiveOrders() {
        Long userId = 1L;
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserIdAndStatusIn(eq(userId), anyList())).thenReturn(false);

        userService.deleteUserIfNoActiveOrders(userId);

        verify(orderRepository).existsByUserIdAndStatusIn(eq(userId), anyList());
        verify(userRepository).delete(user);
    }

    @Test
    void shouldNotDeleteUserIfUserHasActiveOrders() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(orderRepository.existsByUserIdAndStatusIn(eq(userId), anyList())).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class, () -> userService.deleteUserIfNoActiveOrders(userId)
        );

        assertEquals("User cannot be deleted because they have active orders.", exception.getMessage());

        verify(orderRepository).existsByUserIdAndStatusIn(eq(userId), anyList());
        verify(userRepository, never()).delete(any(User.class));
    }


    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.delete(1L));
        assertEquals("User not found", exception.getMessage());

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User(1L, "John", "Doe", "john.doe@example.com", null, LocalDate.of(1999, 11, 11));
        User user2 = new User(2L, "Jane", "Doe", "jane.doe@example.com", null, LocalDate.of(1998, 5, 20));
        List<User> users = List.of(user1, user2);

        UserDto userDto1 = new UserDto(1L, "John", "Doe", "john.doe@example.com", "123456789", null);
        UserDto userDto2 = new UserDto(2L, "Jane", "Doe", "jane.doe@example.com", "987654321", null);
        List<UserDto> userDtos = List.of(userDto1, userDto2);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.converManyToDto(users)).thenReturn(userDtos);

        Iterable<UserDto> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, ((List<UserDto>) result).size());
        verify(userRepository).findAll();
        verify(userMapper).converManyToDto(users);
    }
}