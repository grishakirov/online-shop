package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import cz.cvut.fit.tjv.online_store.domain.*;
import cz.cvut.fit.tjv.online_store.exception.ConflictException;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.OrderRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class UserService implements CrudService<UserDto, Long> {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final BonusCardRepository bonusCardRepository;

    public UserService(UserRepository userRepository, OrderRepository orderRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, BonusCardRepository bonusCardRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.bonusCardRepository = bonusCardRepository;
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


    @Transactional
    public void deleteUserIfNoActiveOrders(Long userId) {
        if(userId == 1){
            throw new IllegalArgumentException("Can't delete admin.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        System.out.println("Trying to delete " + user.getName());
        User defaultUser = getOrCreateDefaultUser();
        if(user.equals(defaultUser)) {
            throw new ConflictException("You can't delete default user");
        }
        List<OrderStatus> activeStatuses = List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED);
        boolean hasActiveOrders = orderRepository.existsByUserIdAndStatusIn(userId, activeStatuses);
        System.out.println("hasActiveOrders: " + hasActiveOrders);

        if (hasActiveOrders) {
            throw new ConflictException("User cannot be deleted because they have active orders.");
        }

        List<OrderStatus> inactiveStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED);
        List<Order> inactiveOrders = orderRepository.findByUserIdAndStatusIn(userId, inactiveStatuses);

        Optional<BonusCard> optionalBonusCard = bonusCardRepository.findByUserId(userId);
        if (optionalBonusCard.isPresent()) {
            BonusCard bonusCard = optionalBonusCard.get();
            bonusCardRepository.delete(bonusCard);
            System.out.println("Deleted bonus card for user " + user.getName());
        }

        if (!inactiveOrders.isEmpty()) {

            for (Order order : inactiveOrders) {
                order.setUser(defaultUser);
                orderRepository.save(order);
            }
            System.out.println("Reassigned " + inactiveOrders.size() + " inactive orders to " + defaultUser.getEmail());
        }

        System.out.println("Deleting " + user.getName());
        userRepository.delete(user);
    }

    public User getOrCreateDefaultUser() {
        String defaultEmail = "deleted@user.com";
        return userRepository.findByEmail(defaultEmail)
                .orElseGet(() -> {
                    User defaultUser = User.builder()
                            .name("Deleted")
                            .surname("User")
                            .email(defaultEmail)
                            .password("password")
                            .role(Role.CUSTOMER)
                            .isActive(false)
                            .build();
                    return userRepository.save(defaultUser);
                });
    }

    public UserDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::convertToDto)
                .orElse(null);
    }
}
