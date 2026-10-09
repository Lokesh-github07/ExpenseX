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

    "/profile"

];

if (protectedPages.includes(window.location.pathname)) {

    if (!isLoggedIn()) {

        window.location.href = "/login";

    }

}