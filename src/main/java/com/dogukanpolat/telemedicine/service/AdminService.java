package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.admin.AdminStatsResponseDto;
import com.dogukanpolat.telemedicine.dto.admin.AiTriageAuditDto;
import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.mappers.AiTriageMapper;
import com.dogukanpolat.telemedicine.mappers.UserMapper;
import com.dogukanpolat.telemedicine.model.UserModel;
import com.dogukanpolat.telemedicine.model.enums.Role;
import com.dogukanpolat.telemedicine.model.enums.UrgencyLevel;
import com.dogukanpolat.telemedicine.repository.AiTriageAuditRepository;
import com.dogukanpolat.telemedicine.repository.AppointmentRepository;
import com.dogukanpolat.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AiTriageAuditRepository aiTriageAuditRepository;

    private final UserMapper userMapper;
    private final AiTriageMapper aiTriageMapper;


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

    public AdminStatsResponseDto getSystemStats() {
        long totalUsers = userRepository.count();
        long totalDoctors = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .count();
        long totalPatients = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.PATIENT)
                .count();
        long totalAppointments = appointmentRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
                .count();

        return new AdminStatsResponseDto(
                totalUsers,
                totalDoctors,
                totalPatients,
                totalAppointments,
                activeUsers
        );
    }

    public List<AiTriageAuditDto> getAiTriageAudit() {
        return aiTriageAuditRepository.findAll().stream()
                .map(aiTriageMapper::toAiTriageAuditDto)
                .toList();
    }

    public List<AiTriageAuditDto> getTriageAuditsByPatient(UUID patientId) {
        return aiTriageAuditRepository.findAll().stream()
                .filter(audit -> audit.getPatient() != null && audit.getPatient().getId().equals(patientId))
                .map(aiTriageMapper::toAiTriageAuditDto)
                .toList();
    }

    public List<AiTriageAuditDto> getTriageAuditsByUrgency(String urgency) {
        UrgencyLevel urgencyLevel = UrgencyLevel.valueOf(urgency.toUpperCase());
        return aiTriageAuditRepository.findAll().stream()
                .filter(audit -> audit.getUrgencyLevel() == urgencyLevel)
                .map(aiTriageMapper::toAiTriageAuditDto)
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
