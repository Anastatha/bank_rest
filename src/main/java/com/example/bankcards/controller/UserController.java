package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResult;
import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.dto.user.CreateUserInput;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Пользователи", description = "Управление пользователями")
@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ApiResult<UserResponse> register(@Parameter(description = "Данные нового пользователя") @RequestBody CreateUserInput input) {
        return userService.register(input);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ApiResult<AuthResponse> login(@Parameter(description = "Данные авторизации") @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        return new ApiResult.Success<>(new AuthResponse(token));
    }

    @Operation(summary = "Получение пользователя по ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResult<UserResponse> getUserById(
            @Parameter(description = "ID пользователя")
            @RequestParam(name = "id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ApiResult.Success<>(UserResponse.fromEntity(user));
    }

    @Operation(summary = "Получение пользователя по username")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-username")
    public ApiResult<UserResponse> getUserByUsername(
            @Parameter(description = "Username пользователя")
            @RequestParam(name = "username") String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ApiResult.Success<>(UserResponse.fromEntity(user));
    }

    @Operation(summary = "Получение пользователя по email")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-email")
    public ApiResult<UserResponse> getUserByEmail(
            @Parameter(description = "Email пользователя")
            @RequestParam(name = "email") String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new ApiResult.Success<>(UserResponse.fromEntity(user));
    }

    @Operation(summary = "Удаление пользователя")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ApiResult<Void> deleteUser(
            @Parameter(description = "ID пользователя")
            @RequestParam(name = "id") Long id) {
        userService.deleteUser(id);
        return new ApiResult.Success<>(null);
    }

    @Operation(summary = "Получение всех пользователей (с пагинацией)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ApiResult<Page<UserResponse>> getAllUsers(@ParameterObject Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsers(pageable)
                .map(UserResponse::fromEntity);
        return new ApiResult.Success<>(response);
    }
}
