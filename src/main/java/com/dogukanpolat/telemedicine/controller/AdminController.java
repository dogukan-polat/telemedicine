package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.admin.UserManagementDto;
import com.dogukanpolat.telemedicine.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserManagementDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<UserManagementDto>> getAllPatients() {
        return ResponseEntity.ok(adminService.getAllPatients());
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<UserManagementDto>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    @PatchMapping("/users/{email}/deactivate")
    public ResponseEntity<UserManagementDto> deactivateUser(@PathVariable String email) {
        return new ResponseEntity<>(adminService.toggleUserActivity(email, false), HttpStatus.OK);
    }

    @PatchMapping("/users/{email}/activate")
    public ResponseEntity<UserManagementDto> activateUser(@PathVariable String email) {
        return new ResponseEntity<>(adminService.toggleUserActivity(email, true), HttpStatus.OK);
    }

    @DeleteMapping("/users/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        adminService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }

}
