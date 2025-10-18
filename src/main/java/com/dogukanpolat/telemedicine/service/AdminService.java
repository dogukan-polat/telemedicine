package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }

    public List<UserManagementDto> getAllUsers() {
       return userRepository.findAll().stream()
               .map(userMapper::toUserManagementDto)
               .toList();
    }

    public List<UserManagementDto> getAllDoctors() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .map(userMapper::toUserManagementDto)
                .toList();
    }

    public List<UserManagementDto> getAllPatients() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.PATIENT)
                .map(userMapper::toUserManagementDto)
                .toList();
    }
}
