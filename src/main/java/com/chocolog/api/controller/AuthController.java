package com.chocolog.api.controller;

import com.chocolog.api.dto.request.LoginRequestDTO;
import com.chocolog.api.dto.response.LoginResponseDTO;
import com.chocolog.api.model.Role;
import com.chocolog.api.security.AppUserDetails;
import com.chocolog.api.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(),
                        loginRequest.getPassword()
                )
        );

        String token = tokenService.generateToken(authentication);
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        Role role = userDetails.getRole();
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .token(token)
                .role(role)
                .build();

        return ResponseEntity.ok(loginResponseDTO);
    }
}