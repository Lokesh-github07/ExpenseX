/* ==========================================
   ExpenseX Financial Overview
   - No fake data (starts empty)
   - Chart built from real transactions only
   - Theme-aware chart colors (reads data-theme)
   - Delete actions for transactions, goals, bills
   - Modals, toasts, empty states
   - Theme toggling is handled by the HTML inline
     script (or script.js); this file only redraws.
========================================== */

(() => {
    "use strict";

    const $ = selector => document.querySelector(selector);

    const money = value =>
        new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            maximumFractionDigits: 0
        }).format(value);

    const today = new Date();

    const isoDate = date =>
        `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;

    const shortDate = date =>
        new Date(`${date}T00:00:00`).toLocaleDateString("en-IN", {
            day: "2-digit",
            month: "short"
        });

    const monthName = date =>
        new Date(`${date}T00:00:00`).toLocaleDateString("en-IN", {
            month: "short"
        });

    const escapeHTML = value =>
        String(value).replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        }[char]));

    // ==========================================
    // STATE (no theme field — handled globally)
    // ==========================================

    const state = {
        period: "month",
        budget: 50000,
        transactions: [],
        goals: [],
        bills: []
    };

    const categoryBudgets = [
        { name: "Housing",   budget: 15000, color: "#2563eb", icon: "⌂" },
        { name: "Food",      budget: 8000,  color: "#059669", icon: "◉" },
        { name: "Transport", budget: 6000,  color: "#d97706", icon: "↗" },
        { name: "Shopping",  budget: 5000,  color: "#0891b2", icon: "◇" },
        { name: "Utilities", budget: 4000,  color: "#64748b", icon: "ϟ" }
    ];

    // ==========================================
    // HELPERS
    // ==========================================

    function getPeriodTransactions() {
        if (state.period === "month") {
            return state.transactions.filter(tx => {
                const d = new Date(`${tx.date}T00:00:00`);
                return d.getMonth() === today.getMonth() &&
                    d.getFullYear() === today.getFullYear();
            });
        }

        const months = state.period === "quarter" ? 3 : 12;
        const start = new Date(today.getFullYear(), today.getMonth() - months + 1, 1);

        return state.transactions.filter(tx =>
            new Date(`${tx.date}T00:00:00`) >= start
        );
    }

    function calculateTotals() {
        const transactions = getPeriodTransactions();

        const income = transactions
            .filter(tx => tx.type === "income")
            .reduce((sum, tx) => sum + tx.amount, 0);

        const expenses = transactions
            .filter(tx => tx.type === "expense")
            .reduce((sum, tx) => sum + tx.amount, 0);

        return { income, expenses, net: income - expenses };
    }

    function showToast(message) {
        const toast = $("#toast");
        toast.textContent = message;
        toast.classList.add("show");

        clearTimeout(showToast.timer);
        showToast.timer = setTimeout(() => {
            toast.classList.remove("show");
        }, 2800);
    }

    // ==========================================
    // RENDER — SUMMARY
    // ==========================================

    function renderSummary() {
        const totals = calculateTotals();

        $("#totalBalance").textContent = money(totals.net);
        $("#totalIncome").textContent = money(totals.income);
        $("#totalExpenses").textContent = money(totals.expenses);

        const savingsRate = totals.income
            ? Math.max(0, totals.net / totals.income * 100)
            : 0;

        $("#savingsRate").textContent = `${savingsRate.toFixed(1)}%`;
        $("#savingsProgress").style.width = `${Math.min(100, savingsRate)}%`;

        $("#netCashFlow").textContent =
            `${totals.net >= 0 ? "+" : "−"}${money(Math.abs(totals.net))}`;

        $("#donutTotal").textContent = money(totals.expenses);

        $("#budgetTotal").textContent = money(state.budget);
        $("#budgetRemaining").textContent =
            money(Math.max(0, state.budget - totals.expenses));

        const used = state.budget
            ? Math.min(100, totals.expenses / state.budget * 100)
            : 0;

        $("#budgetProgress").style.width = `${used}%`;
        $("#budgetUsedLabel").textContent = `${Math.round(used)}% used`;
        $("#budgetLeftLabel").textContent =
            `${Math.max(0, Math.round(100 - used))}% remaining`;

        drawCashflowChart();
    }

    // ==========================================
    // RENDER — CHART
    // ==========================================

    function drawCashflowChart() {
        const canvas = $("#cashflowChart");
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        if (!rect.width || !rect.height) return;

        const dpr = window.devicePixelRatio || 1;
        canvas.width = Math.round(rect.width * dpr);
        canvas.height = Math.round(rect.height * dpr);

        const ctx = canvas.getContext("2d");
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

        const width = rect.width;
        const height = rect.height;

        // Read theme from <html data-theme="...">
        const isDark =
            document.documentElement.getAttribute("data-theme") === "dark";

        const gridColor    = isDark ? "#2a2a2a" : "#e5e9f0";
        const axisColor    = isDark ? "#a3a3a3" : "#718096";
        const incomeColor  = isDark ? "#4ade80" : "#059669";
        const expenseColor = isDark ? "#ff5c6c" : "#e11d48";

        ctx.clearRect(0, 0, width, height);

        const txs = getPeriodTransactions();

        // ---- Empty state ----
        if (!txs.length) {
            ctx.strokeStyle = gridColor;
            ctx.lineWidth = 1;
            for (let i = 0; i < 4; i++) {
                const y = 20 + (height - 50) * i / 3;
                ctx.beginPath();
                ctx.moveTo(40, y);
                ctx.lineTo(width - 10, y);
                ctx.stroke();
            }
            ctx.fillStyle = axisColor;
            ctx.font = "12px Poppins, sans-serif";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText("No data for the selected period", width / 2, height / 2);
            $("#chartNote").textContent = "No data for this period";
            return;
        }

        // ---- Bucket transactions into 12 points ----
        const pointCount = 12;
        const incomeSeries  = new Array(pointCount).fill(0);
        const expenseSeries = new Array(pointCount).fill(0);

        const now = new Date();
        let start, end;
        if (state.period === "month") {
            start = new Date(now.getFullYear(), now.getMonth(), 1);
            end   = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        } else if (state.period === "quarter") {
            start = new Date(now.getFullYear(), now.getMonth() - 2, 1);
            end   = now;
        } else {
            start = new Date(now.getFullYear(), now.getMonth() - 11, 1);
            end   = now;
        }

        const totalMs = end - start || 1;

        txs.forEach(tx => {
            const d = new Date(`${tx.date}T00:00:00`);
            const ratio = Math.min(0.999, Math.max(0, (d - start) / totalMs));
            const idx = Math.floor(ratio * pointCount);
            if (tx.type === "income") incomeSeries[idx] += tx.amount;
            else expenseSeries[idx] += tx.amount;
        });

        const maxVal = Math.max(1, ...incomeSeries, ...expenseSeries);

        const pad = { left: 42, right: 12, top: 14, bottom: 28 };
        const chartW = width - pad.left - pad.right;
        const chartH = height - pad.top - pad.bottom;

        // Grid + Y labels
        ctx.font = "10px Poppins, sans-serif";
        ctx.textBaseline = "middle";

        for (let i = 0; i < 4; i++) {
            const y = pad.top + chartH * i / 3;
            ctx.beginPath();
            ctx.strokeStyle = gridColor;
            ctx.moveTo(pad.left, y);
            ctx.lineTo(width - pad.right, y);
            ctx.stroke();

            const value = maxVal * (3 - i) / 3;
            ctx.fillStyle = axisColor;
            ctx.textAlign = "right";
            ctx.fillText(
                value >= 1000 ? `${(value / 1000).toFixed(0)}k` : value.toFixed(0),
                pad.left - 6,
                y
            );
        }

        // X labels
        const labelCount = Math.min(pointCount, 6);
        for (let i = 0; i < labelCount; i++) {
            const ratio = labelCount === 1 ? 0 : i / (labelCount - 1);
            const x = pad.left + chartW * ratio;
            ctx.fillStyle = axisColor;
            ctx.textAlign = "center";
            ctx.fillText(`P${i + 1}`, x, height - 10);
        }

        // Line drawer
        function drawLine(values, color) {
            ctx.beginPath();
            values.forEach((value, index) => {
                const x = pad.left + chartW * index / (values.length - 1);
                const y = pad.top + chartH * (1 - value / maxVal);
                if (index === 0) ctx.moveTo(x, y);
                else ctx.lineTo(x, y);
            });
            ctx.strokeStyle = color;
            ctx.lineWidth = 2.5;
            ctx.lineJoin = "round";
            ctx.lineCap = "round";
            ctx.stroke();

            values.forEach((value, index) => {
                const x = pad.left + chartW * index / (values.length - 1);
                const y = pad.top + chartH * (1 - value / maxVal);
                ctx.beginPath();
                ctx.arc(x, y, 2.5, 0, Math.PI * 2);
                ctx.fillStyle = color;
                ctx.fill();
            });
        }

        drawLine(incomeSeries, incomeColor);
        drawLine(expenseSeries, expenseColor);

        $("#chartNote").textContent = "Based on your transactions";
    }

    // ==========================================
    // RENDER — CATEGORIES
    // ==========================================

    function renderCategories() {
        const expenses = getPeriodTransactions().filter(tx => tx.type === "expense");

        const totals = {};
        expenses.forEach(tx => {
            totals[tx.category] = (totals[tx.category] || 0) + tx.amount;
        });

        const entries = Object.entries(totals).sort((a, b) => b[1] - a[1]);
        const total = entries.reduce((sum, [, v]) => sum + v, 0);

        if (!total) {
            $("#donutChart").style.background = "var(--input-border)";
            $("#categoryLegend").innerHTML =
                '<div class="empty-state">No expenses yet. Add a transaction to see the breakdown.</div>';
            $("#highestCategory").textContent = "—";
            return;
        }

        const palette = [
            "#2563eb", "#059669", "#d97706", "#0891b2",
            "#7c3aed", "#e11d48", "#64748b"
        ];

        let cursor = 0;
        const gradient = entries.map(([, v], i) => {
            const start = cursor;
            cursor += v / total * 100;
            const color = palette[i % palette.length];
            return `${color} ${start}% ${cursor}%`;
        });

        $("#donutChart").style.background = `conic-gradient(${gradient.join(",")})`;

        $("#categoryLegend").innerHTML = entries.map(([name, amount], i) => {
            const pct = Math.round(amount / total * 100);
            const color = palette[i % palette.length];
            return `
                <div class="category-item">
                    <i class="cat-dot" style="background:${color}"></i>
                    <span class="cat-name">${escapeHTML(name)}</span>
                    <strong>${pct}%</strong>
                </div>
            `;
        }).join("");

        $("#highestCategory").textContent =
            `${entries[0][0]} · ${money(entries[0][1])}`;
    }

    // ==========================================
    // RENDER — BUDGET LIST
    // ==========================================

    function renderBudgetList() {
        const expenses = getPeriodTransactions().filter(tx => tx.type === "expense");

        $("#budgetList").innerHTML = categoryBudgets.map(category => {
            const spent = expenses
                .filter(tx => tx.category === category.name)
                .reduce((sum, tx) => sum + tx.amount, 0);

            const percentage = Math.min(100, spent / category.budget * 100);

            return `
                <div class="budget-item">
                    <div class="budget-item-top">
                        <span><i class="budget-emoji">${category.icon}</i> ${escapeHTML(category.name)}</span>
                        <strong>${money(spent)} <small>/ ${money(category.budget)}</small></strong>
                    </div>
                    <div class="budget-item-track">
                        <span style="width:${percentage}%;background:${category.color}"></span>
                    </div>
                </div>
            `;
        }).join("");
    }

    // ==========================================
    // RENDER — GOALS
    // ==========================================

    function renderGoals() {
        $("#goalList").innerHTML = state.goals.length
            ? state.goals.map((goal, index) => {
                const percentage = Math.min(
                    100,
                    Math.round(goal.saved / goal.target * 100)
                );

                return `
                    <div class="goal-row" data-index="${index}">
                        <div class="goal-icon">${goal.icon}</div>
                        <div class="goal-main">
                            <div class="goal-title-row">
                                <strong>${escapeHTML(goal.name)}</strong>
                                <span class="goal-percent">${percentage}%</span>
                            </div>
                            <div class="goal-amounts">
                                <span>${money(goal.saved)} saved</span>
                                <span>of ${money(goal.target)}</span>
                            </div>
                            <div class="goal-track">
                                <span style="width:${percentage}%"></span>
                            </div>
                        </div>
                        <button type="button"
                                class="icon-btn danger"
                                data-action="delete-goal"
                                data-index="${index}"
                                aria-label="Delete goal"
                                title="Delete goal">✕</button>
                    </div>
                `;
            }).join("")
            : '<div class="empty-state">No savings goals yet. Click "Add goal" to create one.</div>';
    }

    // ==========================================
    // RENDER — TRANSACTIONS
    // ==========================================

    function renderTransactions() {
        const transactions = [...getPeriodTransactions()]
            .sort((a, b) => b.date.localeCompare(a.date))
            .slice(0, 6);

        $("#transactionRows").innerHTML = transactions.length
            ? transactions.map(tx => `
                <tr>
                    <td>
                        <div class="transaction-name">
                            <span class="transaction-icon">${tx.icon || "↔"}</span>
                            <span>
                                ${escapeHTML(tx.name)}
                                <small>${tx.type === "income" ? "Money received" : "Money spent"}</small>
                            </span>
                        </div>
                    </td>
                    <td><span class="category-pill">${escapeHTML(tx.category)}</span></td>
                    <td>${shortDate(tx.date)}</td>
                    <td class="align-right ${tx.type === "income" ? "amount-income" : "amount-expense"}">
                        ${tx.type === "income" ? "+" : "−"}${money(tx.amount)}
                    </td>
                    <td class="align-right">
                        <button type="button"
                                class="icon-btn danger"
                                data-action="delete-transaction"
                                data-id="${tx.id}"
                                aria-label="Delete transaction"
                                title="Delete transaction">✕</button>
                    </td>
                </tr>
            `).join("")
            : '<tr><td colspan="5" class="empty-state">No transactions yet. Click "Add transaction" to record one.</td></tr>';
    }

    // ==========================================
    // RENDER — BILLS
    // ==========================================

    function renderBills() {
        const bills = [...state.bills].sort((a, b) => a.date.localeCompare(b.date));

        $("#billList").innerHTML = bills.length
            ? bills.map((bill, index) => {
                const date = new Date(`${bill.date}T00:00:00`);
                return `
                    <div class="bill-row">
                        <div class="bill-date">
                            <strong>${date.getDate()}</strong>
                            <span>${monthName(bill.date)}</span>
                        </div>
                        <div class="bill-copy">
                            <strong>${escapeHTML(bill.name)}</strong>
                            <span>Due ${shortDate(bill.date)}</span>
                        </div>
                        <strong class="bill-amount">${money(bill.amount)}</strong>
                        <button type="button"
                                class="icon-btn danger"
                                data-action="delete-bill"
                                data-index="${index}"
                                aria-label="Delete bill"
                                title="Delete bill">✕</button>
                    </div>
                `;
            }).join("")
            : '<div class="empty-state">No upcoming bills yet. Click "Add bill" to add one.</div>';

        const total = bills.reduce((sum, bill) => sum + bill.amount, 0);
        $("#upcomingTotal").textContent = money(total);
    }

    // ==========================================
    // RENDER ALL
    // ==========================================

    function renderAll() {
        renderSummary();
        renderCategories();
        renderBudgetList();
        renderGoals();
        renderTransactions();
        renderBills();
    }

    // ==========================================
    // MODAL HELPERS
    // ==========================================

    const openModal = id => $(id).showModal();
    const closeModal = id => $(id).close();

    // ==========================================
    // INIT
    // ==========================================

    $("#currentDate").textContent = today.toLocaleDateString("en-IN", {
        weekday: "short",
        day: "numeric",
        month: "short",
        year: "numeric"
    });

    $("#txDate").value = isoDate(today);
    $("#billDate").value = isoDate(
        new Date(today.getFullYear(), today.getMonth(), Math.min(today.getDate() + 7, 28))
    );

    renderAll();

    // Redraw chart whenever the theme changes (HTML toggle dispatches this)
    window.addEventListener("expensex-theme-change", function () {
        requestAnimationFrame(drawCashflowChart);
    });

    // ==========================================
    // EVENTS
    // ==========================================

    $("#periodSelect").addEventListener("change", event => {
        state.period = event.target.value;
        renderAll();
    });

    document.querySelectorAll("[data-close]").forEach(button => {
        button.addEventListener("click", () => {
            closeModal(`#${button.dataset.close}`);
        });
    });

    // Add transaction
    $("#addTransactionBtn").addEventListener("click", () => {
        openModal("#transactionModal");
    });

    $("#transactionForm").addEventListener("submit", event => {
        event.preventDefault();

        const name = $("#txName").value.trim();
        const amount = Number($("#txAmount").value);
        const type = $("#txType").value;

        if (!name || !Number.isFinite(amount) || amount <= 0) {
            showToast("Enter a valid transaction name and amount.");
            return;
        }

        state.transactions.push({
            id: `tx_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
            name,
            amount,
            type,
            category: $("#txCategory").value,
            date: $("#txDate").value || isoDate(today),
            icon: type === "income" ? "↙" : "↔"
        });

        closeModal("#transactionModal");
        event.target.reset();
        $("#txDate").value = isoDate(today);

        renderAll();
        showToast("Transaction added.");
    });

    // Edit budget
    $("#editBudgetBtn").addEventListener("click", () => {
        $("#budgetInput").value = state.budget;
        openModal("#budgetModal");
    });

    $("#budgetForm").addEventListener("submit", event => {
        event.preventDefault();

        const amount = Number($("#budgetInput").value);

        if (!Number.isFinite(amount) || amount <= 0) {
            showToast("Enter a valid budget amount.");
            return;
        }

        state.budget = amount;
        closeModal("#budgetModal");
        renderAll();
        showToast("Monthly budget updated.");
    });

    // Add goal
    $("#addGoalBtn").addEventListener("click", () => {
        openModal("#goalModal");
    });

    $("#goalForm").addEventListener("submit", event => {
        event.preventDefault();

        const name = $("#goalName").value.trim();
        const target = Number($("#goalTarget").value);
        const saved = Number($("#goalSaved").value);

        if (!name || target <= 0 || saved < 0) {
            showToast("Enter valid financial goal details.");
            return;
        }

        state.goals.push({ name, target, saved, icon: "◎" });
        closeModal("#goalModal");
        event.target.reset();

        renderGoals();
        showToast("Financial goal added.");
    });

    // Add bill
    $("#addBillBtn").addEventListener("click", () => {
        openModal("#billModal");
    });

    $("#billForm").addEventListener("submit", event => {
        event.preventDefault();

        const name = $("#billName").value.trim();
        const amount = Number($("#billAmount").value);
        const date = $("#billDate").value;

        if (!name || amount <= 0 || !date) {
            showToast("Enter valid bill details.");
            return;
        }

        state.bills.push({ name, amount, date });
        closeModal("#billModal");
        event.target.reset();

        renderBills();
        showToast("Upcoming bill added.");
    });

    // Delete actions (delegated)
    document.addEventListener("click", event => {
        const btn = event.target.closest("[data-action]");
        if (!btn) return;

        const action = btn.dataset.action;

        if (action === "delete-transaction") {
            const id = btn.dataset.id;
            state.transactions = state.transactions.filter(tx => tx.id !== id);
            renderAll();
            showToast("Transaction deleted.");
        }

        if (action === "delete-goal") {
            const index = Number(btn.dataset.index);
            state.goals.splice(index, 1);
            renderGoals();
            showToast("Goal deleted.");
        }

        if (action === "delete-bill") {
            const index = Number(btn.dataset.index);
            state.bills.splice(index, 1);
            renderBills();
            showToast("Bill deleted.");
        }
    });

    window.addEventListener("resize", () => {
        requestAnimationFrame(drawCashflowChart);
    });
})();