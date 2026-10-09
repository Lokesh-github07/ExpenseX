package com.example.expensetracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/terms")
    public String terms() {  return "terms"; }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "index";
    }

    @GetMapping("/add-expense")
    public String addExpense() {
        return "add-expense";
    }

    @GetMapping("/edit-expense")
    public String editExpense() {
        return "edit-expense";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "analytics";
    }
}

