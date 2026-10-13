// ============================================================
// ExpenseX — financial-calendar.js
// ============================================================

(function () {
    "use strict";

    // ----------------------------
    // State
    // ----------------------------

    const state = {
        currentMonth: new Date(),
        selectedDate: null,
        category: "all",
        events: []
    };

    // ----------------------------
    // DOM references (resolved on init)
    // ----------------------------

    let dom = {};

    function cacheDom() {
        dom = {
            monthLabel: document.getElementById("monthLabel"),
            calendarGrid: document.getElementById("calendarGrid"),
            prevBtn: document.getElementById("prevBtn"),
            nextBtn: document.getElementById("nextBtn"),
            todayBtn: document.getElementById("todayBtn"),
            refreshBtn: document.getElementById("refreshBtn"),
            categorySelect: document.getElementById("category"),
            status: document.getElementById("status"),
            eventList: document.getElementById("eventList"),
            selectedTitle: document.getElementById("selectedTitle"),
            reminderForm: document.getElementById("reminderForm"),
            reminderTitle: document.getElementById("reminderTitle"),
            reminderDate: document.getElementById("reminderDate"),
            reminderType: document.getElementById("reminderType"),
            reminderAmount: document.getElementById("reminderAmount")
        };
    }

    // ----------------------------
    // Auth + fetch helpers
    // ----------------------------

    function getToken() {
        return localStorage.getItem("token");
    }

    async function apiGet(path) {
        const token = getToken();
        if (!token) {
            window.location = "/login";
            return null;
        }

        try {
            const res = await fetch("/api" + path, {
                method: "GET",
                headers: { "Authorization": "Bearer " + token }
            });

            if (res.status === 401) {
                localStorage.removeItem("token");
                window.location = "/login";
                return null;
            }

            if (!res.ok) {
                console.error("GET " + path + " → " + res.status);
                return null;
            }

            const text = await res.text();
            if (!text) return null;

            try { return JSON.parse(text); }
            catch (e) { console.error("Bad JSON from " + path, text); return null; }

        } catch (err) {
            console.error("GET " + path + " network error:", err);
            return null;
        }
    }

    async function apiPost(path, body) {
        const token = getToken();
        if (!token) {
            window.location = "/login";
            return null;
        }

        try {
            const res = await fetch("/api" + path, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify(body)
            });

            if (res.status === 401) {
                localStorage.removeItem("token");
                window.location = "/login";
                return null;
            }

            return res;

        } catch (err) {
            console.error("POST " + path + " network error:", err);
            return null;
        }
    }

    async function apiDelete(path) {
        const token = getToken();
        if (!token) {
            window.location = "/login";
            return null;
        }

        try {
            const res = await fetch("/api" + path, {
                method: "DELETE",
                headers: { "Authorization": "Bearer " + token }
            });

            if (res.status === 401) {
                localStorage.removeItem("token");
                window.location = "/login";
                return null;
            }

            return res;

        } catch (err) {
            console.error("DELETE " + path + " network error:", err);
            return null;
        }
    }

    // ----------------------------
    // Date helpers
    // ----------------------------

    function startOfMonth(d) {
        return new Date(d.getFullYear(), d.getMonth(), 1);
    }

    function endOfMonth(d) {
        return new Date(d.getFullYear(), d.getMonth() + 1, 0);
    }

    function sameDay(a, b) {
        return a.getFullYear() === b.getFullYear()
            && a.getMonth() === b.getMonth()
            && a.getDate() === b.getDate();
    }

    function isoDate(d) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, "0");
        const day = String(d.getDate()).padStart(2, "0");
        return y + "-" + m + "-" + day;
    }

    function formatMonthLabel(d) {
        return d.toLocaleDateString("en-IN", {
            month: "long",
            year: "numeric"
        });
    }

    function formatEventDate(iso) {
        if (!iso) return "";
        const d = new Date(iso);
        if (isNaN(d)) return iso;
        return d.toLocaleDateString("en-IN", {
            weekday: "short",
            day: "numeric",
            month: "short",
            year: "numeric"
        });
    }

    // ----------------------------
    // Status
    // ----------------------------

    function setStatus(message, isError) {
        if (!dom.status) return;
        dom.status.textContent = message || "";
        dom.status.classList.toggle("error", !!isError);
    }

    // ----------------------------
    // Category → CSS class
    // ----------------------------

    function eventClassForCategory(category) {
        const c = (category || "").toLowerCase();

        if (c === "economic") return "fc-economic";
        if (c === "earnings") return "fc-earnings";
        if (c === "dividend") return "fc-dividend";
        if (c === "ipo") return "fc-ipo";
        if (c === "personal") return "fc-personal";

        // Reminder types map into personal styling
        return "fc-personal";
    }

    // ----------------------------
    // Load events (reminders + anything the API returns)
    // ----------------------------

    async function loadEvents() {
        setStatus("Loading calendar...", false);

        // The app currently stores reminders. If your backend exposes
        // other endpoints (economic events, earnings, etc.), fetch them
        // here and concat into the same array.
        const reminders = await apiGet("/reminders");

        // If the endpoint doesn't exist yet, reminders will be null.
        // That's fine — we render an empty calendar.
        const list = Array.isArray(reminders) ? reminders : [];

        state.events = list.map(function (r) {
            return {
                id: r.id,
                title: r.title || "Reminder",
                date: r.date || r.reminderDate || r.dueDate || "",
                category: (r.type || r.category || "personal").toLowerCase(),
                amount: r.amount != null ? Number(r.amount) : null,
                note: r.note || ""
            };
        }).filter(function (e) { return !!e.date; });

        setStatus(
            state.events.length
                ? state.events.length + " event(s) this month"
                : "No events yet. Add a reminder to get started.",
            false
        );

        renderCalendar();
        renderEventList();
    }

    // ----------------------------
    // Calendar rendering
    // ----------------------------

    function getEventsForDay(day) {
        return state.events.filter(function (e) {
            const d = new Date(e.date);
            return !isNaN(d) && sameDay(d, day);
        });
    }

    function renderCalendar() {

        if (!dom.calendarGrid || !dom.monthLabel) return;

        const month = state.currentMonth;
        const first = startOfMonth(month);
        const last = endOfMonth(month);

        dom.monthLabel.textContent = formatMonthLabel(month);

        // Grid starts on Sunday of the week containing the 1st
        const startOffset = first.getDay(); // 0 = Sun
        const gridStart = new Date(first);
        gridStart.setDate(first.getDate() - startOffset);

        // End on Saturday of the week containing the last day
        const endOffset = last.getDay();
        const gridEnd = new Date(last);
        gridEnd.setDate(last.getDate() + (6 - endOffset));

        const today = new Date();

        const frag = document.createDocumentFragment();

        const cursor = new Date(gridStart);
        while (cursor <= gridEnd) {

            const day = new Date(cursor);

            const cell = document.createElement("div");
            cell.className = "fc-day";
            cell.setAttribute("role", "button");
            cell.setAttribute("tabindex", "0");
            cell.dataset.date = isoDate(day);

            // Outside current month?
            if (day.getMonth() !== month.getMonth()) {
                cell.classList.add("outside");
            }

            // Today?
            if (sameDay(day, today)) {
                cell.classList.add("today");
            }

            // Selected?
            if (state.selectedDate && sameDay(day, state.selectedDate)) {
                cell.classList.add("selected");
            }

            // Day number
            const num = document.createElement("span");
            num.className = "fc-day-number";
            num.textContent = day.getDate();
            cell.appendChild(num);

            // Events for this day
            const dayEvents = getEventsForDay(day);

            dayEvents.forEach(function (ev) {

                // Filter by category
                if (state.category !== "all"
                    && ev.category !== state.category) {
                    return;
                }

                const dot = document.createElement("div");
                dot.className = "fc-event-dot " + eventClassForCategory(ev.category);
                dot.textContent = ev.title;
                dot.title = ev.title;
                cell.appendChild(dot);
            });

            // Click handler
            cell.addEventListener("click", function () {
                state.selectedDate = day;
                renderCalendar();
                renderEventList();
            });

            // Keyboard (Enter / Space)
            cell.addEventListener("keydown", function (e) {
                if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    state.selectedDate = day;
                    renderCalendar();
                    renderEventList();
                }
            });

            frag.appendChild(cell);

            cursor.setDate(cursor.getDate() + 1);
        }

        dom.calendarGrid.innerHTML = "";
        dom.calendarGrid.appendChild(frag);
    }

    // ----------------------------
    // Event list (right column)
    // ----------------------------

    function renderEventList() {

        if (!dom.eventList) return;

        // If a date is selected, filter to that date; otherwise show all
        let list = state.events.slice();

        if (state.selectedDate) {
            list = list.filter(function (e) {
                const d = new Date(e.date);
                return !isNaN(d) && sameDay(d, state.selectedDate);
            });
        }

        if (state.category !== "all") {
            list = list.filter(function (e) {
                return e.category === state.category;
            });
        }

        // Update heading
        if (dom.selectedTitle) {
            if (state.selectedDate) {
                dom.selectedTitle.textContent =
                    "Events · " + state.selectedDate.toLocaleDateString(
                        "en-IN",
                        { day: "numeric", month: "short", year: "numeric" }
                    );
            } else {
                dom.selectedTitle.textContent = "Events";
            }
        }

        dom.eventList.innerHTML = "";

        if (list.length === 0) {
            const empty = document.createElement("div");
            empty.className = "fc-empty";
            empty.textContent = state.selectedDate
                ? "No events on this day."
                : "No events yet.";
            dom.eventList.appendChild(empty);
            return;
        }

        list.sort(function (a, b) {
            return new Date(a.date) - new Date(b.date);
        });

        list.forEach(function (ev) {

            const card = document.createElement("div");
            card.className = "fc-event-card";

            const title = document.createElement("strong");
            title.textContent = ev.title;
            card.appendChild(title);

            if (ev.amount != null && !isNaN(ev.amount)) {
                const amt = document.createElement("p");
                amt.textContent = "Amount: ₹" + ev.amount.toFixed(2);
                card.appendChild(amt);
            }

            const dateEl = document.createElement("p");
            dateEl.className = "fc-event-date";
            dateEl.textContent = formatEventDate(ev.date);
            card.appendChild(dateEl);

            // Delete button
            const del = document.createElement("button");
            del.type = "button";
            del.className = "fc-delete";
            del.textContent = "Delete";
            del.addEventListener("click", function (e) {
                e.stopPropagation();
                deleteEvent(ev.id);
            });
            card.appendChild(del);

            dom.eventList.appendChild(card);
        });
    }

    // ----------------------------
    // Create / delete reminders
    // ----------------------------

    async function handleReminderSubmit(e) {

        e.preventDefault();

        if (!dom.reminderForm) return;

        const title = (dom.reminderTitle.value || "").trim();
        const date = (dom.reminderDate.value || "").trim();
        const type = (dom.reminderType.value || "personal").toLowerCase();
        const amountRaw = (dom.reminderAmount.value || "").trim();

        if (!title || !date) {
            setStatus("Please provide a title and date.", true);
            return;
        }

        const payload = {
            title: title,
            date: date,
            type: type,
            amount: amountRaw === "" ? null : Number(amountRaw)
        };

        setStatus("Saving reminder...", false);

        const res = await apiPost("/reminders", payload);

        if (!res) {
            setStatus("Could not save the reminder. Please try again.", true);
            return;
        }

        if (!res.ok) {
            let msg = "Could not save the reminder.";
            try {
                const t = await res.text();
                if (t) msg = t;
            } catch (e) { /* ignore */ }
            setStatus(msg, true);
            return;
        }

        // Reset form
        dom.reminderForm.reset();

        setStatus("Reminder saved.", false);

        await loadEvents();
    }

    async function deleteEvent(id) {

        if (!id) return;

        const ok = confirm("Delete this reminder?");
        if (!ok) return;

        const res = await apiDelete("/reminders/" + id);

        if (!res) {
            setStatus("Could not delete the reminder.", true);
            return;
        }

        if (!res.ok) {
            setStatus("Could not delete the reminder.", true);
            return;
        }

        await loadEvents();
    }

    // ----------------------------
    // Navigation
    // ----------------------------

    function goPrevMonth() {
        state.currentMonth = new Date(
            state.currentMonth.getFullYear(),
            state.currentMonth.getMonth() - 1,
            1
        );
        state.selectedDate = null;
        renderCalendar();
        renderEventList();
    }

    function goNextMonth() {
        state.currentMonth = new Date(
            state.currentMonth.getFullYear(),
            state.currentMonth.getMonth() + 1,
            1
        );
        state.selectedDate = null;
        renderCalendar();
        renderEventList();
    }

    function goToday() {
        state.currentMonth = new Date();
        state.selectedDate = new Date();
        renderCalendar();
        renderEventList();
    }

    // ----------------------------
    // Event wiring
    // ----------------------------

    function wireEvents() {

        if (dom.prevBtn) dom.prevBtn.addEventListener("click", goPrevMonth);
        if (dom.nextBtn) dom.nextBtn.addEventListener("click", goNextMonth);
        if (dom.todayBtn) dom.todayBtn.addEventListener("click", goToday);

        if (dom.refreshBtn) {
            dom.refreshBtn.addEventListener("click", function () {
                loadEvents();
            });
        }

        if (dom.categorySelect) {
            dom.categorySelect.addEventListener("change", function () {
                state.category = dom.categorySelect.value || "all";
                renderCalendar();
                renderEventList();
            });
        }

        if (dom.reminderForm) {
            dom.reminderForm.addEventListener("submit", handleReminderSubmit);
        }
    }

    // ----------------------------
    // Init
    // ----------------------------

    function init() {

        cacheDom();

        if (!dom.calendarGrid) {
            console.error("[FinancialCalendar] #calendarGrid not found");
            return;
        }

        state.selectedDate = new Date();

        wireEvents();

        // Immediately render an empty grid so it doesn't say "Loading..."
        renderCalendar();
        renderEventList();

        // Then fetch the real events
        loadEvents();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

})();