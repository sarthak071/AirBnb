package com.sarthak.airbnb.controller;

import com.sarthak.airbnb.dto.LoginDto;
import com.sarthak.airbnb.dto.LoginResponseDto;
import com.sarthak.airbnb.dto.SignUpRequestDto;
import com.sarthak.airbnb.dto.UserDto;
import com.sarthak.airbnb.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Create a new account", tags = {"Auth"})
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto)
    {
        return new ResponseEntity<>(authService.signUp(signUpRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login request", tags = {"Auth"})
    public  ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto, HttpServletRequest req, HttpServletResponse res){
        String[] tokens = authService.login(loginDto);
        Cookie cookie = new Cookie("refreshToken", tokens[1]);
        cookie.setHttpOnly(true);
        res.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDto(tokens[0]));

    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh the JWT with a refresh token", tags = {"Auth"})
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found in Cookie"));

        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }
}
