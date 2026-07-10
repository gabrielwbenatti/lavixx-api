package com.benattidev.lavixx.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.benattidev.lavixx.dto.user.InviteResponse;
import com.benattidev.lavixx.dto.user.InviteUserRequest;
import com.benattidev.lavixx.dto.user.UpdateUserActiveRequest;
import com.benattidev.lavixx.dto.user.UpdateUserRoleRequest;
import com.benattidev.lavixx.dto.user.UserResponse;
import com.benattidev.lavixx.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userService.list());
    }

    @PostMapping
    public ResponseEntity<InviteResponse> invite(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.invite(request));
    }

    @PostMapping("/{id}/resend-invite")
    public ResponseEntity<InviteResponse> resendInvite(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.resendInvite(id));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request.role()));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> setActive(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateUserActiveRequest request) {
        return ResponseEntity.ok(userService.setActive(id, request.active()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
