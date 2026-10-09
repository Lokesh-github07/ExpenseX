package com.example.expensetracker.service.impl;

import com.example.expensetracker.dto.request.UpdateProfileRequest;
import com.example.expensetracker.dto.response.ApiResponse;
import com.example.expensetracker.dto.response.UserResponse;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.NotificationRepository;
import com.example.expensetracker.repository.RefreshTokenRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.UserService;
import com.example.expensetracker.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.example.expensetracker.dto.request.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final ExpenseRepository expenseRepository;

    private final CategoryRepository categoryRepository;

    private final BudgetRepository budgetRepository;

    private final NotificationRepository notificationRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;



    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user ->
                        modelMapper.map(
                                user,
                                UserResponse.class
                        )
                )
                .toList();
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Override
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return modelMapper.map(
                user,
                UserResponse.class
        );
    }


    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    @Override
    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return modelMapper.map(
                user,
                UserResponse.class
        );
    }


    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @Override
    public UserResponse updateProfile(
            Long id,
            UpdateProfileRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        if (!ValidationUtil.isNullOrEmpty(
                request.getFirstName())) {

            user.setFirstName(
                    request.getFirstName().trim()
            );
        }

        if (!ValidationUtil.isNullOrEmpty(
                request.getLastName())) {

            user.setLastName(
                    request.getLastName().trim()
            );
        }

        // Phone number is optional
        if (request.getPhoneNumber() != null) {

            user.setPhoneNumber(
                    request.getPhoneNumber().trim()
            );
        }

        User saved = userRepository.save(user);

        return modelMapper.map(
                saved,
                UserResponse.class
        );
    }


    // =========================================================
    // DELETE USER BY ID
    // =========================================================

    @Override
    @Transactional
    public ApiResponse deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        deleteUserData(id);

        userRepository.delete(user);

        return new ApiResponse(
                true,
                "User deleted successfully"
        );
    }


    // =========================================================
    // DELETE CURRENTLY LOGGED-IN USER ACCOUNT
    // =========================================================

    @Override
    @Transactional
    public ApiResponse deleteMyAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        Long userId = user.getId();

        /*
         * IMPORTANT:
         *
         * User is the parent record.
         *
         * These tables contain foreign keys pointing
         * to users.id.
         *
         * Therefore they MUST be deleted first.
         */

        deleteUserData(userId);

        // Finally delete the parent user.
        userRepository.delete(user);

        return new ApiResponse(
                true,
                "Account deleted permanently"
        );
    }


    // =========================================================
    // DELETE ALL DATA BELONGING TO USER
    // =========================================================

    private void deleteUserData(Long userId) {

        /*
         * 1. Refresh tokens
         */
        refreshTokenRepository.deleteByUserId(userId);


        /*
         * 2. Notifications
         */
        notificationRepository.deleteByUserId(userId);


        /*
         * 3. Budgets
         */
        budgetRepository.deleteByUserId(userId);


        /*
         * 4. Expenses
         *
         * Expenses are deleted before categories because
         * expenses can reference categories.
         */
        expenseRepository.deleteByUserId(userId);


        /*
         * 5. Categories
         */
        categoryRepository.deleteByUserId(userId);
    }


    // =========================================================
    // ACTIVATE USER
    // =========================================================

    @Override
    public ApiResponse activateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setActive(true);

        userRepository.save(user);

        return new ApiResponse(
                true,
                "User activated successfully"
        );
    }


    // =========================================================
    // DEACTIVATE USER
    // =========================================================

    @Override
    public ApiResponse deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setActive(false);

        userRepository.save(user);

        return new ApiResponse(
                true,
                "User deactivated successfully"
        );
    }
    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Override
    @Transactional
    public ApiResponse changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            return new ApiResponse(
                    false,
                    "Current password is incorrect"
            );
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            return new ApiResponse(
                    false,
                    "New password and confirm password do not match"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            return new ApiResponse(
                    false,
                    "New password must be different from the current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        return new ApiResponse(
                true,
                "Password changed successfully"
        );
    }
}
