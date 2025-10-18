package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }
}
