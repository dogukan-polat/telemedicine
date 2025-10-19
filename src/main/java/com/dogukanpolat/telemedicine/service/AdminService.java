package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

    @Transactional
    public UserManagementDto toggleUserActivity(String email, boolean isActive) {
        Optional<UserModel> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("User with email " + email + " not found");
        }
        UserModel actualUser = user.get();
        actualUser.setIsActive(isActive);
        userRepository.save(actualUser);
        return userMapper.toUserManagementDto(actualUser);
    }

    @Transactional
    public void deleteUser(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " not found");
        }
        userRepository.deleteByEmail(email);
    }
}
