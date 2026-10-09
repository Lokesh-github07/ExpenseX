package com.example.expensetracker.repository;

import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUser(User user);

    // Used when permanently deleting a user account
    void deleteByUserId(Long userId);
}