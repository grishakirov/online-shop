package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private OrderRepository orderRepository;

    @Mock
    private BonusCardRepository bonusCardRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "raw_password",
                LocalDate.of(1999, 11, 11)
        );
        testUserDto = new UserDto(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "raw_password",
                null,
                LocalDate.of(1999, 11, 11)
        );
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userMapper.convertToEntity(testUserDto)).thenReturn(testUser);
        when(passwordEncoder.encode("raw_password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.convertToDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.save(testUserDto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userMapper).convertToEntity(testUserDto);
        verify(passwordEncoder).encode("raw_password");
        verify(userRepository).save(testUser);
        verify(userMapper).convertToDto(testUser);
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.save(testUserDto)
        );
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).findByEmail("john.doe@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldFindAllUsers() {
        User user2 = new User(
                2L,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "secret",
                LocalDate.of(2000, 2, 2)
        );

        List<User> userList = List.of(testUser, user2);
        UserDto userDto2 = new UserDto(
                2L,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "secret",
                null,
                LocalDate.of(2000, 2, 2)
        );

        when(userRepository.findAll()).thenReturn(userList);
        when(userMapper.convertManyToDto(userList))
                .thenReturn(List.of(testUserDto, userDto2));

        Iterable<UserDto> result = userService.findAll();

        List<UserDto> resultList = (List<UserDto>) result;
        assertEquals(2, resultList.size());
        assertEquals("John", resultList.get(0).getName());
        assertEquals("Jane", resultList.get(1).getName());
    }

    @Test
    void shouldReturnUserWhenIdExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.convertToDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userRepository).findById(1L);
        verify(userMapper).convertToDto(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.findById(999L)
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userMapper.convertToEntity(testUserDto)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.convertToDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.update(1L, testUserDto);

        assertNotNull(result);
        assertEquals("John", result.getName());
        verify(userRepository).existsById(1L);
        verify(userMapper).convertToEntity(testUserDto);
        verify(userRepository).save(testUser);
        verify(userMapper).convertToDto(testUser);
    }

    @Test
    void shouldDeleteUserIfNoActiveOrders() {
        Long userId = 2L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(orderRepository.existsByUserIdAndStatusIn(eq(userId), anyList()))
                .thenReturn(false);
        when(orderRepository.findByUserIdAndStatusIn(eq(userId), anyList()))
                .thenReturn(List.of());
        when(bonusCardRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        doNothing().when(userRepository).delete(testUser);

        userService.deleteUserIfNoActiveOrders(userId);

        verify(userRepository).delete(testUser);
    }

    @Test
    void shouldNotDeleteUserIfHasActiveOrders() {
        Long userId = 2L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        when(orderRepository.existsByUserIdAndStatusIn(eq(userId), anyList()))
                .thenReturn(true);
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.deleteUserIfNoActiveOrders(userId)
        );
        assertEquals("User cannot be deleted because they have active orders.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(55L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.delete(55L)
        );
        assertEquals("User not found", exception.getMessage());

        verify(userRepository).existsById(55L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}