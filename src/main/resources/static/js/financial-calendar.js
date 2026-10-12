// ===========================
// Financial Calendar
// ===========================

const SERIES = {
    GDP: "GDP",
    CPIAUCSL: "Consumer Price Index",
    UNRATE: "Unemployment rate",
    FEDFUNDS: "Fed funds rate"
};
const REMINDER_KEY = "expensex_reminders";

const today = new Date();
const state = {
    year: today.getFullYear(),
    month: today.getMonth(),          // 0-11
    selected: toISO(today),
    category: "all",
    economic: [],
    reminders: loadReminders()
};

const el = id => document.getElementById(id);

function pad(n) { return String(n).padStart(2, "0"); }
function toISO(d) { return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`; }

function setStatus(msg) {
    const s = el("status");
    if (s) s.textContent = msg;
}

// ---------- Reminders (stored in this browser only) ----------
function loadReminders() {
    try { return JSON.parse(localStorage.getItem(REMINDER_KEY)) || []; }
    catch { return []; }
}
function saveReminders() {
    localStorage.setItem(REMINDER_KEY, JSON.stringify(state.reminders));
}

// ---------- Economic data from backend (FRED) ----------
async function fetchSeries(seriesId) {
    const res = await fetch(
        `/api/financial-calendar/economic-data?seriesId=${encodeURIComponent(seriesId)}`,
        { headers: authHeaders() }
    );
    if (res.status === 401) { logout(); return []; }
    if (!res.ok) throw new Error(`${seriesId}: ${res.status}`);

    const data = await res.json();
    return (data.observations || [])
        .filter(o => o.value !== ".")                // FRED uses "." for missing values
        .map(o => ({
            id: `${seriesId}-${o.date}`,
            date: o.date,
            title: `${SERIES[seriesId]}: ${o.value}`,
            category: "economic"
        }));
}

async function loadEconomic() {
    setStatus("Loading economic data...");
    const results = await Promise.allSettled(Object.keys(SERIES).map(fetchSeries));

    state.economic = results
        .filter(r => r.status === "fulfilled")
        .flatMap(r => r.value);

    const failed = results.filter(r => r.status === "rejected").length;
    setStatus(failed
        ? `Loaded with ${failed} failed request(s). Check the server log / FRED API key.`
        : "Economic data loaded.");

    render();
}

// ---------- Helpers ----------
function allEvents() {
    const reminders = state.reminders.map(r => ({
        id: r.id,
        date: r.date,
        title: r.amount ? `${r.title} (₹${r.amount})` : r.title,
        category: "personal"
    }));
    return [...state.economic, ...reminders].filter(
        e => state.category === "all" || e.category === state.category
    );
}

// ---------- Rendering ----------
function renderCalendar() {
    const grid = el("calendarGrid");
    grid.replaceChildren();

    const first = new Date(state.year, state.month, 1);
    el("monthLabel").textContent =
        first.toLocaleString("en-IN", { month: "long", year: "numeric" });

    const daysInMonth = new Date(state.year, state.month + 1, 0).getDate();
    const events = allEvents();

    for (let i = 0; i < first.getDay(); i++) {
        grid.appendChild(document.createElement("div"));   // empty leading cells
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const iso = `${state.year}-${pad(state.month + 1)}-${pad(day)}`;
        const count = events.filter(e => e.date === iso).length;

        const cell = document.createElement("button");
        cell.type = "button";
        cell.className = "fc-day";
        if (iso === toISO(today)) cell.classList.add("today");
        if (iso === state.selected) cell.classList.add("selected");
        cell.textContent = count ? `${day} •${count}` : String(day);
        cell.addEventListener("click", () => { state.selected = iso; render(); });
        grid.appendChild(cell);
    }
}

function renderEvents() {
    el("selectedTitle").textContent = `Events · ${state.selected}`;
    const list = el("eventList");
    list.replaceChildren();

    const items = allEvents().filter(e => e.date === state.selected);
    if (!items.length) {
        list.textContent = "No events on this date.";
        return;
    }

    items.forEach(e => {
        const row = document.createElement("div");
        row.className = "fc-event";
        row.textContent = `${e.category === "economic" ? "📊" : "🔔"} ${e.title}`;   // textContent = no XSS

        if (e.category === "personal") {
            const del = document.createElement("button");
            del.type = "button";
            del.textContent = " ✕";
            del.addEventListener("click", () => {
                state.reminders = state.reminders.filter(r => r.id !== e.id);
                saveReminders();
                render();
            });
            row.appendChild(del);
        }
        list.appendChild(row);
    });
}

function render() {
    renderCalendar();
    renderEvents();
}

// ---------- Wiring ----------
function changeMonth(delta) {
    const d = new Date(state.year, state.month + delta, 1);
    state.year = d.getFullYear();
    state.month = d.getMonth();
    render();
}

document.addEventListener("DOMContentLoaded", () => {
    el("prevBtn").addEventListener("click", () => changeMonth(-1));
    el("nextBtn").addEventListener("click", () => changeMonth(1));

    el("todayBtn").addEventListener("click", () => {
        state.year = today.getFullYear();
        state.month = today.getMonth();
        state.selected = toISO(today);
        render();
    });

    el("category").addEventListener("change", e => {
        state.category = e.target.value;
        render();
    });

    el("refreshBtn").addEventListener("click", loadEconomic);

    el("reminderForm").addEventListener("submit", e => {
        e.preventDefault();
        const f = e.target;
        state.reminders.push({
            id: String(Date.now()),
            title: f.title.value.trim(),
            date: f.date.value,
            type: f.type.value,
            amount: f.amount.value
        });
        saveReminders();
        f.reset();
        render();
    });

    // Sidebar dropdowns (script.js has no code for these)
    document.querySelectorAll(".popup-toggle").forEach(btn => {
        btn.addEventListener("click", () => {
            const popup = btn.nextElementSibling;
            const open = popup.hidden;
            popup.hidden = !open;
            btn.setAttribute("aria-expanded", String(open));
        });
    });

    // Floating quick-action button (same reason)
    const fab = el("floatingAddButton");
    const menu = el("quickActionMenu");
    fab.addEventListener("click", () => {
        menu.hidden = !menu.hidden;
        fab.setAttribute("aria-expanded", String(!menu.hidden));
    });

    render();
    loadEconomic();
});