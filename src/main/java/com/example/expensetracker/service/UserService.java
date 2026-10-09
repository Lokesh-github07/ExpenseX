package com.example.expensetracker.service;

import com.example.expensetracker.dto.request.UpdateProfileRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.UserResponse;
import com.example.expensetracker.dto.request.ChangePasswordRequest;
import java.util.List;

public interface UserService {

    // =========================================================
    // GET ALL USERS
    // =========================================================

    List<UserResponse> getAllUsers();


    // =========================================================
    // GET USER BY ID
    // =========================================================

    UserResponse getUserById(Long id);


    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    UserResponse getUserByEmail(String email);


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    UserResponse updateProfile(
            Long id,
            UpdateProfileRequest request
    );

    // =========================================================
    // CHANGE PASSWORD (logged-in user)
    // =========================================================
    //
    // Requires the user's current password for verification.
    // The email comes from Spring Security Authentication.
    //
    // =========================================================

    ApiResponse changePassword(
            String email,
            ChangePasswordRequest request
    );

    // =========================================================
    // DELETE USER BY ID
    // =========================================================
    //
    // Used for admin/user-management functionality.
    //
    // =========================================================

    ApiResponse deleteUser(Long id);


    // =========================================================
    // DELETE CURRENTLY LOGGED-IN USER ACCOUNT
    // =========================================================
    //
    // Used by the Profile page.
    //
    // The email comes from Spring Security Authentication.
    //
    // =========================================================

    ApiResponse deleteMyAccount(String email);


    // =========================================================
    // ACTIVATE USER
    // =========================================================

    ApiResponse activateUser(Long id);


    // =========================================================
    // DEACTIVATE USER
    // =========================================================

    ApiResponse deactivateUser(Long id);

}