package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResult;
import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.user.CreateUserInput;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void register_ShouldReturnUserResponse() throws Exception {
        CreateUserInput input = new CreateUserInput("john@mail.com", "123456", "john");
        UserResponse response = new UserResponse("john", "john@mail.com", Role.USER);
        when(userService.register(any(CreateUserInput.class)))
                .thenReturn(new ApiResult.Success<>(response));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.email").value("john@mail.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void login_ShouldReturnAuthResponse() throws Exception {
        AuthRequest request = new AuthRequest("john@mail.com", "123456");
        Authentication auth = new UsernamePasswordAuthenticationToken("john@mail.com", "123456");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("token123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token123"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setRole(Role.USER);
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/user").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.email").value("john@mail.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void getUserByUsername_ShouldReturnUser() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setRole(Role.USER);
        when(userService.findByUsername("john")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/user/by-username").param("username", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.email").value("john@mail.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void getUserByEmail_ShouldReturnUser() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setRole(Role.USER);
        when(userService.findByEmail("john@mail.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/user/by-email").param("email", "john@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.email").value("john@mail.com"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void deleteUser_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/user").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getAllUsers_ShouldReturnPage() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setEmail("john@mail.com");
        user.setRole(Role.USER);

        when(userService.getAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("john"))
                .andExpect(jsonPath("$.data.content[0].email").value("john@mail.com"))
                .andExpect(jsonPath("$.data.content[0].role").value("USER"));
    }
}
