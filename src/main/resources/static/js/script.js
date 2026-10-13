
/* ==========================================
   ExpenseX - script.js
   Shared theme, authentication, API helpers,
   and dashboard integration
========================================== */

/* ==========================================
   GLOBAL THEME MANAGER
========================================== */

(function () {
    const THEME_KEY = "expensex-theme";

    try {
        const saved =
            localStorage.getItem(THEME_KEY) ||
            localStorage.getItem("theme") ||
            "light";

        document.documentElement.setAttribute(
            "data-theme",
            saved
        );
    } catch (error) {
        // Storage may be unavailable.
    }

    window.ExpenseXTheme = {
        key: THEME_KEY,

        get: function () {
            return document.documentElement.getAttribute(
                "data-theme"
            ) || "light";
        },

        set: function (theme) {
            if (theme !== "light" && theme !== "dark") {
                return;
            }

            document.documentElement.setAttribute(
                "data-theme",
                theme
            );

            document.documentElement.dataset.theme = theme;

            if (document.body) {
                document.body.setAttribute(
                    "data-theme",
                    theme
                );
            }

            try {
                localStorage.setItem(THEME_KEY, theme);
            } catch (error) {
                // Ignore storage errors.
            }

            const button =
                document.getElementById("themeToggle");

            if (button) {
                button.setAttribute(
                    "aria-label",
                    "Switch to " +
                    (theme === "dark" ? "light" : "dark") +
                    " mode"
                );
            }

            window.dispatchEvent(
                new CustomEvent("expensex-theme-change", {
                    detail: { theme: theme }
                })
            );
        },

        toggle: function () {
            this.set(
                this.get() === "dark" ? "light" : "dark"
            );
        }
    };

    document.addEventListener("DOMContentLoaded", function () {
        const button =
            document.getElementById("themeToggle");

        if (!button || button.dataset.themeWired) {
            return;
        }

        button.dataset.themeWired = "1";

        button.addEventListener("click", function (event) {
            event.preventDefault();
            window.ExpenseXTheme.toggle();
        });
    });
})();

/* ==========================================
   API CONFIGURATION
========================================== */

const API_BASE = "/api";

/* ==========================================
   JWT HELPERS
========================================== */

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

/* ==========================================
   AUTHENTICATION HEADERS
========================================== */

function authHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getToken()
    };
}

/* ==========================================
   GET REQUEST
========================================== */

async function getRequest(url) {
    const response = await fetch(API_BASE + url, {
        method: "GET",
        headers: authHeaders()
    });

    if (response.status === 401) {
        logout();
        return null;
    }

    if (!response.ok) {
        throw new Error("GET request failed: " + response.status);
    }

    return response.json();
}

/* ==========================================
   POST REQUEST
========================================== */

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

/* ==========================================
   PUT REQUEST
========================================== */

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

/* ==========================================
   DELETE REQUEST
========================================== */

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

/* ==========================================
   FORMATTERS
========================================== */

function formatCurrency(value) {
    return "₹" + Number(value || 0).toLocaleString(
        "en-IN",
        {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }
    );
}

function formatDate(date) {
    if (!date) {
        return "";
    }

    const parsedDate = new Date(date);

    if (Number.isNaN(parsedDate.getTime())) {
        return "";
    }

    return parsedDate.toLocaleDateString("en-IN");
}

function showMessage(message) {
    alert(message);
}

/* ==========================================
   PROTECTED PAGES
========================================== */

const protectedPages = [
    "/dashboard",
    "/add-expense",
    "/edit-expense",
    "/analytics",
    "/profile",
    "/financial-calendar"
];

if (
    protectedPages.includes(window.location.pathname) &&
    !isLoggedIn()
) {
    window.location.replace("/login");
}

/* ==========================================
   SIDEBAR CONTROLS
   Matches the supplied dashboard HTML.
========================================== */

document.addEventListener("DOMContentLoaded", function () {
    const dashboard = document.querySelector(".dashboard");
    const sidebar = document.getElementById("appSidebar");
    const desktopToggle =
        document.getElementById("sidebarToggle");
    const mobileToggle =
        document.getElementById("mobileSidebarToggle");

    if (!dashboard || !sidebar) {
        return;
    }

    function setSidebarCollapsed(collapsed) {
        dashboard.classList.toggle(
            "sidebar-collapsed",
            collapsed
        );

        sidebar.classList.toggle(
            "is-collapsed",
            collapsed
        );

        if (desktopToggle) {
            desktopToggle.setAttribute(
                "aria-expanded",
                String(!collapsed)
            );

            desktopToggle.setAttribute(
                "aria-label",
                collapsed
                    ? "Expand sidebar"
                    : "Collapse sidebar"
            );
        }
    }

    function setMobileSidebarOpen(open) {
        sidebar.classList.toggle(
            "is-mobile-open",
            open
        );

        if (mobileToggle) {
            mobileToggle.setAttribute(
                "aria-expanded",
                String(open)
            );

            mobileToggle.setAttribute(
                "aria-label",
                open
                    ? "Close navigation"
                    : "Open navigation"
            );
        }
    }

    if (desktopToggle) {
        desktopToggle.addEventListener("click", function () {
            setSidebarCollapsed(
                !dashboard.classList.contains(
                    "sidebar-collapsed"
                )
            );
        });
    }

    if (mobileToggle) {
        mobileToggle.addEventListener("click", function () {
            setMobileSidebarOpen(
                !sidebar.classList.contains(
                    "is-mobile-open"
                )
            );
        });
    }

    document.addEventListener("click", function (event) {
        if (
            sidebar.classList.contains("is-mobile-open") &&
            !sidebar.contains(event.target) &&
            !(mobileToggle &&
                mobileToggle.contains(event.target))
        ) {
            setMobileSidebarOpen(false);
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            setMobileSidebarOpen(false);
        }
    });

    sidebar.querySelectorAll("nav a").forEach(function (link) {
        link.addEventListener("click", function () {
            setMobileSidebarOpen(false);
        });
    });

    setSidebarCollapsed(false);
});

/* ==========================================
   DASHBOARD INITIALIZATION
========================================== */

// Keep your existing dashboard functions here:
// 1. loadExpenses()
// 2. deleteExpense(expenseId)
// 3. escapeHtml(value)
// 4. loadCategoryPieChart()
//
// Preserve the existing API routes in those functions:
//
// GET    /api/expenses
// DELETE /api/expenses/{id}
// GET    /api/users/email/{email}
// GET    /api/reports/category-summary?userId={id}
//
// Keep the existing Chart.js configuration and webhook
// URL logic from your dashboard HTML.
//
// Initialize dashboard data only on the dashboard page,
// after verifying the token and required HTML elements.
