const output = document.getElementById("output");
const apiBaseUrlLabel = document.getElementById("apiBaseUrlLabel");
const apiBaseUrl = window.APP_CONFIG?.apiBaseUrl || "";

apiBaseUrlLabel.textContent = apiBaseUrl || "missing in config.js";

function setOutput(data) {
    output.textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2);
}

function getToken() {
    return sessionStorage.getItem("authToken");
}

function setToken(token) {
    sessionStorage.setItem("authToken", token);
}

function clearToken() {
    sessionStorage.removeItem("authToken");
}

async function callApi(path, options = {}) {
    if (!apiBaseUrl) {
        throw new Error("config.js is missing apiBaseUrl");
    }

    const headers = new Headers(options.headers || {});
    headers.set("Content-Type", "application/json");

    const token = getToken();
    if (token) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${apiBaseUrl}${path}`, {
        ...options,
        headers
    });

    const text = await response.text();
    let body;

    try {
        body = text ? JSON.parse(text) : {};
    } catch (error) {
        body = { raw: text };
    }

    if (!response.ok) {
        throw new Error(JSON.stringify(body));
    }

    return body;
}

document.getElementById("registerForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    try {
        const payload = {
            username: document.getElementById("registerUsername").value.trim(),
            password: document.getElementById("registerPassword").value
        };

        const result = await callApi("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        setOutput(result);
    } catch (error) {
        setOutput(error.message);
    }
});

document.getElementById("loginForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    try {
        const payload = {
            username: document.getElementById("loginUsername").value.trim(),
            password: document.getElementById("loginPassword").value
        };

        const result = await callApi("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        if (result.token) {
            setToken(result.token);
        }

        setOutput(result);
    } catch (error) {
        setOutput(error.message);
    }
});

document.getElementById("secureButton").addEventListener("click", async () => {
    try {
        const result = await callApi("/api/secure/hello");
        setOutput(result);
    } catch (error) {
        setOutput(error.message);
    }
});

document.getElementById("logoutButton").addEventListener("click", async () => {
    try {
        const result = await callApi("/api/auth/logout", {
            method: "POST"
        });
        clearToken();
        setOutput(result);
    } catch (error) {
        clearToken();
        setOutput(error.message);
    }
});
