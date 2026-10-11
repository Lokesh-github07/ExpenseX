// ================================
// Expense Tracker - script.js
// ================================

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
// Auth Headers
// ----------------------------
// GET / DELETE: only Authorization.
// POST / PUT: add Content-Type (JSON body).

function authHeaders() {
    return {
        "Authorization": "Bearer " + getToken()
    };
}

function jsonAuthHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getToken()
    };
}

// ----------------------------
// Safe JSON parse
// ----------------------------

async function parseJsonSafely(response, url) {

    const text = await response.text();

    if (!text) return null;

    try {
        return JSON.parse(text);
    } catch (error) {
        console.error("Invalid JSON from " + url + ":", text);
        return null;
    }
}

// ----------------------------
// GET
// ----------------------------

async function getRequest(url) {

    try {
        const response = await fetch(API_BASE + url, {
            method: "GET",
            headers: authHeaders()
        });

        if (response.status === 401) {
            logout();
            return null;
        }

        if (!response.ok) {
            console.error("GET " + url + " failed: " + response.status);
            return null;
        }

        return await parseJsonSafely(response, url);

    } catch (error) {
        console.error("GET " + url + " network error:", error);
        return null;
    }
}

// ----------------------------
// POST
// ----------------------------

async function postRequest(url, data) {

    try {
        const response = await fetch(API_BASE + url, {
            method: "POST",
            headers: jsonAuthHeaders(),
            body: JSON.stringify(data)
        });

        if (response.status === 401) {
            logout();
        }

        return response;

    } catch (error) {
        console.error("POST " + url + " network error:", error);
        throw error;
    }
}

// ----------------------------
// PUT
// ----------------------------

async function putRequest(url, data) {

    try {
        const response = await fetch(API_BASE + url, {
            method: "PUT",
            headers: jsonAuthHeaders(),
            body: JSON.stringify(data)
        });

        if (response.status === 401) {
            logout();
        }

        return response;

    } catch (error) {
        console.error("PUT " + url + " network error:", error);
        throw error;
    }
}

// ----------------------------
// DELETE
// ----------------------------

async function deleteRequest(url) {

    try {
        const response = await fetch(API_BASE + url, {
            method: "DELETE",
            headers: authHeaders()
        });

        if (response.status === 401) {
            logout();
        }

        return response;

    } catch (error) {
        console.error("DELETE " + url + " network error:", error);
        throw error;
    }
}

// ----------------------------
// Formatters
// ----------------------------

function formatCurrency(value) {
    return "₹" + Number(value).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatDate(date) {
    if (!date) return "";
    return new Date(date).toLocaleDateString("en-IN");
}

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

    if (!confirmed) return;

    try {
        const response = await deleteRequest("/expenses/" + expenseId);

        if (response.ok) {

            alert("Expense deleted successfully.");

            if (typeof loadExpenses === "function") {
                loadExpenses();
            } else {
                window.location.reload();
            }

        } else {

            let message = "Failed to delete expense.";

            try {
                const text = await response.text();
                if (text) message = text;
            } catch (error) {
                // keep default
            }

            alert(message);
        }

    } catch (error) {
        console.error("Delete expense error:", error);
        alert("Unable to delete the expense. Please try again.");
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
    "/financial-calendar"
];

if (protectedPages.includes(window.location.pathname)) {
    if (!isLoggedIn()) {
        window.location.href = "/login";
    }
}

// ================================
// THEME TOGGLE (global — one source of truth)
// ================================

function applyTheme(theme) {

    const isLight = theme === "light";

    document.documentElement.dataset.theme = theme;

    if (document.body) {
        document.body.dataset.theme = theme;
    }

    document.querySelectorAll(".theme-toggle").forEach(function (btn) {

        btn.textContent = isLight ? "🌙" : "☀️";

        btn.setAttribute(
            "aria-label",
            isLight
                ? "Switch to dark theme"
                : "Switch to light theme"
        );

        btn.setAttribute(
            "title",
            isLight
                ? "Switch to dark theme"
                : "Switch to light theme"
        );
    });

    const meta = document.getElementById("themeColor");

    if (meta) {
        meta.setAttribute(
            "content",
            isLight ? "#f4f7fb" : "#10151f"
        );
    }

    // Optional in-header labels (kept for compatibility with any page
    // that might still render #themeIcon / #themeText)
    const icon = document.getElementById("themeIcon");
    const text = document.getElementById("themeText");

    if (icon) icon.textContent = isLight ? "🌙" : "☀️";
    if (text) text.textContent = isLight ? "Dark mode" : "Light mode";
}

function toggleTheme() {

    const current = document.documentElement.dataset.theme;

    const next = current === "dark" ? "light" : "dark";

    applyTheme(next);

    try {
        localStorage.setItem("expenseXTheme", next);
    } catch (error) {
        // ignore
    }
}

function bindThemeToggle(btn) {

    if (!btn || btn.dataset.themeBound === "true") return;

    btn.dataset.themeBound = "true";

    btn.addEventListener("click", function (event) {
        event.preventDefault();
        toggleTheme();
    });
}

function ensureThemeToggleExists() {

    // Already present — nothing to do.
    if (document.querySelector(".theme-toggle")) return;

    // Only inject on protected pages (skip login / register / terms).
    if (!protectedPages.includes(window.location.pathname)) return;

    const btn = document.createElement("button");

    btn.type = "button";
    btn.className = "theme-toggle";
    btn.setAttribute("aria-label", "Toggle theme");
    btn.setAttribute("title", "Toggle theme");

    document.body.appendChild(btn);
}

function initTheme() {

    // 1. Apply saved / default theme
    let saved = null;

    try {
        saved = localStorage.getItem("expenseXTheme");
    } catch (error) {
        saved = null;
    }

    applyTheme(saved === "light" ? "light" : "dark");

    // 2. Ensure a floating toggle exists on protected pages
    ensureThemeToggleExists();

    // 3. Bind click handlers to every toggle currently in the DOM
    document.querySelectorAll(".theme-toggle").forEach(function (btn) {
        bindThemeToggle(btn);
    });
}

// Expose so pages can call these manually
window.applyTheme  = applyTheme;
window.toggleTheme = toggleTheme;
window.initTheme   = initTheme;

// Run on DOM ready
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initTheme);
} else {
    initTheme();
}

// ----------------------------
// Global error logging
// ----------------------------

window.addEventListener("error", function (event) {
    console.error(
        "[ExpenseX] Uncaught error:",
        event.message,
        event.filename
            ? "(" + event.filename + ":" + event.lineno + ")"
            : ""
    );
});

window.addEventListener("unhandledrejection", function (event) {
    console.error("[ExpenseX] Unhandled promise rejection:", event.reason);
});