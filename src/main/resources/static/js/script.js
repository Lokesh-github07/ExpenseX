/* ==========================================
   ExpenseX - script.js
   Shared authentication, API helpers,
   sidebar controls, and section-page helpers
   (Transactions, Income & Budget, Bills, Savings)
========================================== */

/* ==========================================
   THEME
   ExpenseX uses ONE permanent dark theme (see the
   design tokens in css/style.css). There is no theme
   switching and nothing is read from localStorage.
   The only work here is removing keys that older
   versions of the app saved in the browser.
========================================== */

(function () {
    try {
        localStorage.removeItem("expensex-theme");
        localStorage.removeItem("theme");
    } catch (error) {
        // Storage may be unavailable; nothing to clean up.
    }
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

// The ONE logout implementation. It is also called automatically when the
// API answers 401, so it must not ask for confirmation - use
// confirmLogout() for buttons the user clicks.
function logout() {
    removeToken();

    try {
        // Older builds / future token storage - clear any auth leftovers.
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("accessToken");
        sessionStorage.clear();
    } catch (error) {
        // Storage may be unavailable.
    }

    // replace() keeps the protected page out of the Back-button history.
    window.location.replace("/login");
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
    "/financial-calendar",
    "/transactions",
    "/salary",
    "/bills",
    "/savings",
    "/settings",
    "/security-privacy"
];

if (
    protectedPages.includes(window.location.pathname) &&
    !isLoggedIn()
) {
    window.location.replace("/login");
}

// After logout the Back button can restore a cached protected page.
window.addEventListener("pageshow", function (event) {
    if (
        event.persisted &&
        protectedPages.includes(window.location.pathname) &&
        !isLoggedIn()
    ) {
        window.location.replace("/login");
    }
});

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
   SECTION PAGE HELPERS  (window.ExpenseX)
   Used by: transactions, salary, bills, savings
========================================== */

(function () {
    "use strict";

    // ---------- Per-user local storage ----------
    // For data that has no backend endpoint yet
    // (income, budgets, bills, loans, goals, CIBIL).
    // Replace Store calls with fetch() when you add APIs.

    function userId() {
        try {
            const part = String(getToken())
                .split(".")[1]
                .replace(/-/g, "+")
                .replace(/_/g, "/");
            return JSON.parse(atob(part)).sub || "guest";
        } catch (e) {
            return "guest";
        }
    }

    const Store = {
        key: function (name) {
            return "expensex:" + userId() + ":" + name;
        },
        load: function (name, fallback) {
            try {
                const raw = localStorage.getItem(this.key(name));
                return raw ? JSON.parse(raw) : fallback;
            } catch (e) {
                return fallback;
            }
        },
        save: function (name, value) {
            try {
                localStorage.setItem(this.key(name), JSON.stringify(value));
            } catch (e) {
                toast("Could not save data in this browser.", true);
            }
        }
    };

    // ---------- API helper (adds the Bearer token) ----------
    async function api(path, options) {
        options = options || {};

        const headers = Object.assign(
            { Authorization: "Bearer " + getToken() },
            options.headers || {}
        );

        if (options.body && !headers["Content-Type"]) {
            headers["Content-Type"] = "application/json";
        }

        const response = await fetch(
            path,
            Object.assign({}, options, { headers: headers })
        );

        if (response.status === 401) {
            logout();
        }

        return response;
    }

    // ---------- Small utilities ----------
    function money(value) {
        return formatCurrency(value);
    }

    function esc(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function uid() {
        return Date.now().toString(36) +
            Math.random().toString(36).slice(2, 7);
    }

    function today() {
        const d = new Date();
        d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
        return d.toISOString().slice(0, 10);
    }

    function isThisMonth(value) {
        const d = new Date(value);
        const now = new Date();

        return !Number.isNaN(d.getTime()) &&
            d.getFullYear() === now.getFullYear() &&
            d.getMonth() === now.getMonth();
    }

    let toastTimer = null;

    function toast(message, isError) {
        let el = document.getElementById("toast");

        if (!el) {
            el = document.createElement("div");
            el.id = "toast";
            el.className = "toast";
            el.setAttribute("role", "status");
            document.body.appendChild(el);
        }

        el.textContent = message;
        el.classList.toggle("error", !!isError);
        el.classList.add("show");

        clearTimeout(toastTimer);
        toastTimer = setTimeout(function () {
            el.classList.remove("show");
        }, 2600);
    }

    // ---------- Sub-page (view) switching ----------
    // Sidebar links with data-view="id" show
    // <section class="view" id="id"> without leaving the page.
    // The URL hash keeps the selection on refresh.
    function initViews(defaultView) {
        const links = Array.prototype.slice.call(
            document.querySelectorAll("[data-view]")
        );
        const views = Array.prototype.slice.call(
            document.querySelectorAll(".view")
        );
        const title = document.getElementById("pageTitle");
        const content = document.querySelector(".content");

        if (content) {
            content.classList.add("views-ready");
        }

        function show(id, updateHash) {
            if (!views.some(function (v) { return v.id === id; })) {
                id = defaultView;
            }

            views.forEach(function (v) {
                v.classList.toggle("active", v.id === id);
            });

            links.forEach(function (link) {
                const active = link.dataset.view === id;
                link.classList.toggle("active", active);

                if (active) {
                    link.setAttribute("aria-current", "page");

                    if (title) {
                        title.textContent =
                            link.dataset.title || link.textContent.trim();
                    }
                } else {
                    link.removeAttribute("aria-current");
                }
            });

            if (updateHash) {
                history.replaceState(null, "", "#" + id);
            }

            document.dispatchEvent(
                new CustomEvent("viewchange", { detail: { id: id } })
            );

            window.scrollTo({ top: 0 });
        }

        links.forEach(function (link) {
            link.addEventListener("click", function (event) {
                event.preventDefault();
                show(link.dataset.view, true);
            });
        });

        window.addEventListener("hashchange", function () {
            show(location.hash.slice(1), false);
        });

        show(location.hash.slice(1) || defaultView, false);

        return function (id) {
            show(id, true);
        };
    }

    window.ExpenseX = window.ExpenseX || {};

    Object.assign(window.ExpenseX, {
        Store: Store,
        api: api,
        money: money,
        esc: esc,
        uid: uid,
        today: today,
        formatDate: formatDate,
        isThisMonth: isThisMonth,
        toast: toast,
        initViews: initViews
    });
})();

/* ==========================================
   DASHBOARD INITIALIZATION
========================================== */

// Keep your existing dashboard functions in dashboard.html:
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

/* ==========================================
   CONFIRMATION DIALOG, LOGOUT CONFIRMATION
   AND SIDEBAR PROFILE
========================================== */

(function () {
    "use strict";

    // confirmDialog({title, message, confirmText, cancelText, danger, requireText})
    // resolves true only when the user explicitly confirms.
    // requireText: the user must type this exact word to enable Confirm.
    function confirmDialog(options) {
        options = options || {};

        return new Promise(function (resolve) {
            const previouslyFocused = document.activeElement;
            const overlay = document.createElement("div");
            overlay.className = "delete-confirm";

            const dialog = document.createElement("div");
            dialog.className = "delete-dialog";
            dialog.setAttribute("role", "alertdialog");
            dialog.setAttribute("aria-modal", "true");
            dialog.setAttribute("aria-labelledby", "xConfirmTitle");
            dialog.setAttribute("aria-describedby", "xConfirmMessage");

            const title = document.createElement("h3");
            title.id = "xConfirmTitle";
            title.textContent = options.title || "Are you sure?";

            const message = document.createElement("p");
            message.id = "xConfirmMessage";
            message.textContent = options.message || "";

            dialog.appendChild(title);
            dialog.appendChild(message);

            let input = null;

            if (options.requireText) {
                input = document.createElement("input");
                input.type = "text";
                input.autocomplete = "off";
                input.className = "confirm-input";
                input.setAttribute(
                    "aria-label",
                    "Type " + options.requireText + " to confirm"
                );
                input.placeholder = "Type " + options.requireText;
                dialog.appendChild(input);
            }

            const actions = document.createElement("div");
            actions.className = "delete-dialog-actions";

            const cancel = document.createElement("button");
            cancel.type = "button";
            cancel.className = "btn-secondary";
            cancel.textContent = options.cancelText || "Cancel";

            const confirm = document.createElement("button");
            confirm.type = "button";
            confirm.className = options.danger ? "btn-danger" : "btn-primary";
            confirm.textContent = options.confirmText || "Confirm";

            if (input) {
                confirm.disabled = true;
                input.addEventListener("input", function () {
                    confirm.disabled =
                        input.value.trim() !== options.requireText;
                });
            }

            actions.appendChild(cancel);
            actions.appendChild(confirm);
            dialog.appendChild(actions);
            overlay.appendChild(dialog);
            document.body.appendChild(overlay);

            function close(result) {
                document.removeEventListener("keydown", onKey, true);
                overlay.remove();

                if (previouslyFocused && previouslyFocused.focus) {
                    previouslyFocused.focus();
                }

                resolve(result);
            }

            function onKey(event) {
                if (event.key === "Escape") {
                    event.preventDefault();
                    close(false);
                } else if (event.key === "Tab") {
                    // Keep keyboard focus inside the dialog.
                    const focusable = Array.prototype.slice.call(
                        dialog.querySelectorAll("button, input")
                    ).filter(function (el) { return !el.disabled; });

                    if (!focusable.length) { return; }

                    const first = focusable[0];
                    const last = focusable[focusable.length - 1];

                    if (event.shiftKey && document.activeElement === first) {
                        event.preventDefault();
                        last.focus();
                    } else if (!event.shiftKey && document.activeElement === last) {
                        event.preventDefault();
                        first.focus();
                    }
                }
            }

            document.addEventListener("keydown", onKey, true);
            overlay.addEventListener("mousedown", function (event) {
                if (event.target === overlay) { close(false); }
            });
            cancel.addEventListener("click", function () { close(false); });
            confirm.addEventListener("click", function () { close(true); });

            (input || cancel).focus();
        });
    }

    // Asks first, then runs the single logout() implementation.
    function confirmLogout() {
        return confirmDialog({
            title: "Log out of ExpenseX?",
            message: "You will need to sign in again to access your account.",
            confirmText: "Log out",
            cancelText: "Stay signed in",
            danger: true
        }).then(function (ok) {
            if (ok) { logout(); }
            return ok;
        });
    }

    // ---- Sidebar profile: avatar initials + the user's real name ----
    function tokenEmail() {
        try {
            const part = String(getToken()).split(".")[1]
                .replace(/-/g, "+").replace(/_/g, "/");
            return JSON.parse(atob(part.padEnd(Math.ceil(part.length / 4) * 4, "="))).sub || null;
        } catch (e) {
            return null;
        }
    }

    function paintSidebarProfile(first, last) {
        const nameEl = document.getElementById("sidebarProfileName");
        const avatarEl = document.getElementById("sidebarProfileAvatar");
        const full = ((first || "") + " " + (last || "")).trim();

        if (nameEl && full) { nameEl.textContent = full; }

        if (avatarEl && full) {
            const a = (first || "").trim()[0] || "";
            const b = (last || "").trim()[0] || "";
            avatarEl.textContent = (a + b).toUpperCase() || "\u{1F464}";
        }
    }

    async function refreshSidebarProfile() {
        if (!document.getElementById("sidebarProfileName") || !isLoggedIn()) {
            return;
        }

        const email = tokenEmail();
        if (!email) { return; }

        const cacheKey = "expensex:sidebar-name";

        try {
            const cached = JSON.parse(sessionStorage.getItem(cacheKey) || "null");
            if (cached && cached.email === email) {
                paintSidebarProfile(cached.first, cached.last);
            }
        } catch (e) { /* ignore */ }

        try {
            const response = await fetch(
                "/api/users/email/" + encodeURIComponent(email),
                { headers: { Authorization: "Bearer " + getToken() } }
            );

            // Never log out from a cosmetic lookup; the page itself handles auth.
            if (!response.ok) { return; }

            const user = await response.json();
            paintSidebarProfile(user.firstName, user.lastName);

            try {
                sessionStorage.setItem(cacheKey, JSON.stringify({
                    email: email,
                    first: user.firstName || "",
                    last: user.lastName || ""
                }));
            } catch (e) { /* ignore */ }
        } catch (e) {
            // Keep the generic "My Profile" label.
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        // Mark the active sidebar link for assistive technology.
        document.querySelectorAll(".sidebar-menu a.active").forEach(function (a) {
            a.setAttribute("aria-current", "page");
        });

        refreshSidebarProfile();
    });

    window.confirmLogout = confirmLogout;
    window.ExpenseX = window.ExpenseX || {};
    window.ExpenseX.confirmDialog = confirmDialog;
    window.ExpenseX.refreshSidebarProfile = refreshSidebarProfile;
})();
