package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class UserService implements CrudService<UserDto, Long> {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, OrderRepository orderRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean hasActiveOrders(Long userId) {
        return orderRepository.existsByUserIdAndStatusIn(userId, List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED));
    }

    @Override
    public UserDto save(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }
        User user = userMapper.convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.convertToDto(savedUser);
    }

    @Override
    public Iterable<UserDto> findAll() {
        List<User> users = (List<User>) userRepository.findAll();
        return userMapper.convertManyToDto(users);
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.convertToDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userMapper.convertToEntity(userDto);
        user.setId(id);
        User updatedUser = userRepository.save(user);
        return userMapper.convertToDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    public UserDto create(UserDto userDto) {
        User user = userMapper.convertToEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.convertToDto(savedUser);
    }


    public void deleteUserIfNoActiveOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        List<OrderStatus> activeStatuses = List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
        boolean hasActiveOrders = orderRepository.existsByUserIdAndStatusIn(userId, activeStatuses);

        if (hasActiveOrders) {
            throw new ConflictException("User cannot be deleted because they have active orders.");
        }

        userRepository.delete(user);
    }

    public UserDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::convertToDto)
                .orElse(null);
    }
}
