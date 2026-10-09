package com.example.expensetracker.controller;

import com.example.expensetracker.dto.request.UpdateProfileRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.UserResponse;
import com.example.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.expensetracker.dto.request.ChangePasswordRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }


    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        id,
                        request
                )
        );
    }

    // =========================================================
    // CHANGE PASSWORD (logged-in user)
    // =========================================================
    //
    // PUT /api/users/change-password
    //
    // The browser does NOT provide the user ID or old password check state.
    // Spring Security gets the email from the JWT.
    //
    // =========================================================

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.changePassword(email, request)
        );
    }

    // =========================================================
    // DELETE USER BY ID
    // =========================================================
    //
    // Keep this for admin functionality.
    //
    // DELETE /api/users/{id}
    //
    // =========================================================

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse> deleteMyAccount(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.deleteMyAccount(email)
        );
    }


    // =========================================================
    // DELETE CURRENTLY LOGGED-IN USER
    // =========================================================
    //
    // Used by the Profile page.
    //
    // DELETE /api/users/account
    //
    // The browser does NOT provide the user ID.
    //
    // Spring Security gets the email from the JWT.
    //
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.deleteUser(id)
        );
    }



    // =========================================================
    // ACTIVATE USER
    // =========================================================

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse> activateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.activateUser(id)
        );
    }


    // =========================================================
    // DEACTIVATE USER
    // =========================================================

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.deactivateUser(id)
        );
    }
}