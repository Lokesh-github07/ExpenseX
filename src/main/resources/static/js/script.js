// ================================
// Expense Tracker - script.js
// ================================

/* =========================================================
   GLOBAL THEME MANAGER
   Runs first, on every page that loads this script.
   Uses ONE key ("expensex-theme") and ONE attribute
   (<html data-theme="light|dark">) so every page stays
   in sync — Dashboard, Financial Calendar, Financial
   Overview, Profile, Analytics.
========================================================= */

(function () {
    var THEME_KEY = "expensex-theme";

    // Apply saved theme immediately (before paint) to prevent flash
    try {
        var saved = localStorage.getItem(THEME_KEY) || localStorage.getItem("theme") || "light";
        document.documentElement.setAttribute("data-theme", saved);
        document.documentElement.dataset.theme = saved;
    } catch (e) { /* ignore private-mode errors */ }

    // Public helper — other scripts can call window.ExpenseXTheme.set(...)
    window.ExpenseXTheme = {
        key: THEME_KEY,

        get: function () {
            return document.documentElement.getAttribute("data-theme") || "light";
        },

        set: function (theme) {
            document.documentElement.setAttribute("data-theme", theme);
            document.documentElement.dataset.theme = theme;

            // Also mirror on <body> so any page-scoped selector picks it up
            if (document.body) {
                document.body.setAttribute("data-theme", theme);
                document.body.dataset.theme = theme;
            }

            try {
                localStorage.setItem(THEME_KEY, theme);
            } catch (e) { /* ignore */ }

            // Keep any toggle's aria-label in sync
            var btn = document.getElementById("themeToggle");
            if (btn) {
                btn.setAttribute(
                    "aria-label",
                    "Switch to " + (theme === "dark" ? "light" : "dark") + " mode"
                );
            }

            // Notify listeners (e.g. chart redraw)
            window.dispatchEvent(new CustomEvent("expensex-theme-change", {
                detail: { theme: theme }
            }));
        },

        toggle: function () {
            this.set(this.get() === "dark" ? "light" : "dark");
        }
    };

    // Wire up the toggle button on any page that has one
    document.addEventListener("DOMContentLoaded", function () {
        var btn = document.getElementById("themeToggle");
        if (!btn) return;

        // Remove any pre-existing inline handler binding to avoid double toggles
        if (!btn.dataset.themeWired) {
            btn.dataset.themeWired = "1";
            btn.addEventListener("click", function (e) {
                e.preventDefault();
                window.ExpenseXTheme.toggle();
            });
        }

        // Ensure aria-label reflects current state
        btn.setAttribute(
            "aria-label",
            "Switch to " +
            (window.ExpenseXTheme.get() === "dark" ? "light" : "dark") +
            " mode"
        );
    });
})();


const API_BASE = "/api";

// ----------------------------
// JWT Helpers
// ----------------------------

function saveToken(token) {
    localStorage.setItem("token", token);
}

function getToken() {
    return localStorage.getItem("token");
}

function removeToken() {
    localStorage.removeItem("token");
}

function isLoggedIn() {
    return getToken() !== null;
}

function logout() {

    removeToken();

    window.location.href = "/login";

}

// ----------------------------
// Auth Header
// ----------------------------

function authHeaders() {

    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getToken()
    };

}

// ----------------------------
// GET Request
// ----------------------------

async function getRequest(url) {

    const response = await fetch(API_BASE + url, {

        method: "GET",

        headers: authHeaders()

    });

    if (response.status === 401) {

        logout();

        return null;

    }

    return await response.json();

}

// ----------------------------
// POST Request
// ----------------------------

async function postRequest(url, data) {

    const response = await fetch(API_BASE + url, {

        method: "POST",

        headers: authHeaders(),

        body: JSON.stringify(data)

    });

    if (response.status === 401) {

        logout();

    }

    return response;

}

// ----------------------------
// PUT Request
// ----------------------------

async function putRequest(url, data) {

    const response = await fetch(API_BASE + url, {

        method: "PUT",

        headers: authHeaders(),

        body: JSON.stringify(data)

    });

    if (response.status === 401) {

        logout();

    }

    return response;

}

// ----------------------------
// DELETE Request
// ----------------------------

async function deleteRequest(url) {

    const response = await fetch(API_BASE + url, {

        method: "DELETE",

        headers: {
            "Authorization": "Bearer " + getToken()
        }

    });

    if (response.status === 401) {

        logout();

    }

    return response;

}

// ----------------------------
// Currency Formatter
// ----------------------------

function formatCurrency(value) {

    return "₹" + Number(value).toLocaleString("en-IN", {

        minimumFractionDigits: 2,

        maximumFractionDigits: 2

    });

}

// ----------------------------
// Date Formatter
// ----------------------------

function formatDate(date) {

    if (!date) {
        return "";
    }

    return new Date(date).toLocaleDateString("en-IN");

}

// ----------------------------
// Notification
// ----------------------------

function showMessage(message) {

    alert(message);

}

// ----------------------------
// Delete Expense
// ----------------------------

async function deleteExpense(expenseId) {

    if (!expenseId) {

        alert("Invalid expense ID.");

        return;

    }

    const confirmed = confirm(
        "Are you sure you want to delete this expense?"
    );

    if (!confirmed) {

        return;

    }

    try {

        const response = await deleteRequest(
            "/expenses/" + expenseId
        );

        if (response.ok) {

            alert("Expense deleted successfully.");

            // Reload dashboard data
            if (typeof loadExpenses === "function") {

                loadExpenses();

            } else {

                window.location.reload();

            }

        } else {

            let message = "Failed to delete expense.";

            try {

                const text = await response.text();

                if (text) {
                    message = text;
                }

            } catch (error) {
                // Keep default message
            }

            alert(message);

        }

    } catch (error) {

        console.error("Delete expense error:", error);

        alert(
            "Unable to delete the expense. Please try again."
        );

    }

}

// ----------------------------
// Page Protection
// ----------------------------

const protectedPages = [

    "/dashboard",

    "/add-expense",

    "/edit-expense",

    "/analytics",

    "/profile",

    "/financial-calendar",

    "/financial-overview"

];

if (protectedPages.includes(window.location.pathname)) {

    if (!isLoggedIn()) {

        window.location.href = "/login";

    }

}