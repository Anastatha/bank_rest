package com.example.bankcards.service;

import com.example.bankcards.dto.ApiResult;
import com.example.bankcards.dto.user.CreateUserInput;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AlreadyExistsException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john");
        testUser.setEmail("john@mail.com");
        testUser.setPassword("encoded");
        testUser.setRole(Role.USER);
    }

    @Test
    void register_ShouldCreateUser_WhenValidInput() {
        CreateUserInput input = new CreateUserInput("john@mail.com", "123456", "john");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ApiResult<UserResponse> result = userService.register(input);

        assertTrue(result instanceof ApiResult.Success<?>);
        UserResponse response = ((ApiResult.Success<UserResponse>) result).data();
        assertEquals("john", response.username());
        assertEquals("john@mail.com", response.email());
        assertEquals(Role.USER, response.role());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenUsernameExists() {
        CreateUserInput input = new CreateUserInput("john@mail.com", "123456", "john");
        when(userRepository.existsByUsername("john")).thenReturn(true);

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> userService.register(input));
        assertEquals("Username already taken", ex.getMessage());
    }

    @Test
    void register_ShouldThrow_WhenEmailExists() {
        CreateUserInput input = new CreateUserInput("john@mail.com", "123456", "john");
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(true);

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> userService.register(input));
        assertEquals("Email already taken", ex.getMessage());
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<User> user = userService.findById(1L);

        assertTrue(user.isPresent());
        assertEquals("john", user.get().getUsername());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<User> user = userService.findById(2L);

        assertTrue(user.isEmpty());
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
        Optional<User> user = userService.findByUsername("john");

        assertTrue(user.isPresent());
        assertEquals("john", user.get().getUsername());
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNotExists() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());
        Optional<User> user = userService.findByUsername("notfound");

        assertTrue(user.isEmpty());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.of(testUser));
        Optional<User> user = userService.findByEmail("john@mail.com");

        assertTrue(user.isPresent());
        assertEquals("john@mail.com", user.get().getEmail());
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenNotExists() {
        when(userRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());
        Optional<User> user = userService.findByEmail("notfound@mail.com");

        assertTrue(user.isEmpty());
    }

    @Test
    void deleteUser_ShouldDelete_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrow_WhenNotExists() {
        when(userRepository.existsById(2L)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(2L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnPage() {
        List<User> users = Collections.singletonList(testUser);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("john", result.getContent().get(0).getUsername());
    }
}
