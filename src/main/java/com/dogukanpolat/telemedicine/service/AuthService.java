package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.auth.JwtResponseDto;
import com.dogukanpolat.telemedicine.dto.auth.UserLoginDto;
import com.dogukanpolat.telemedicine.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public JwtResponseDto login(UserLoginDto userLoginDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginDto.email(),
                        userLoginDto.password()
                )
        );

        String token = jwtUtils.generateToken(userLoginDto.email());
        return new JwtResponseDto(token);
    }
}
