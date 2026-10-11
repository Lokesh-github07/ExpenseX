
async function fetchFinancialEvents() {
    const status = document.getElementById("status");

    try {
        status.textContent = "Loading financial events...";

        const response = await fetch("/api/financial-calendar", {
            method: "GET",
            headers: {
                "Accept": "application/json",
                ...authHeaders()
            }
        });

        if (response.status === 401) {
            throw new Error(
                "Unauthorized: Please log in again or check your JWT token."
            );
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(
                `Request failed: ${response.status} - ${errorText}`
            );
        }

        const events = await response.json();

        console.log("Financial events:", events);

        status.textContent = "Financial events loaded.";

        // Pass events to your existing calendar rendering function.
        // Example: renderFinancialEvents(events);
        // Use your actual existing function name.

        return events;

    } catch (error) {
        console.error("Financial calendar API error:", error);
        status.textContent = error.message;
        return [];
    }
}
