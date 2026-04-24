const state = {
    accessToken: "",
    refreshToken: "",
    accessTokenExpiresAt: "",
    refreshTokenExpiresAt: ""
};

const accessTokenField = document.getElementById("accessToken");
const refreshTokenField = document.getElementById("refreshToken");
const accessExpireField = document.getElementById("accessExpire");
const refreshExpireField = document.getElementById("refreshExpire");
const responseBox = document.getElementById("responseBox");
const statusCode = document.getElementById("statusCode");
const statusText = document.getElementById("statusText");

document.getElementById("loginButton").addEventListener("click", login);
document.getElementById("messageButton").addEventListener("click", getMessage);
document.getElementById("refreshButton").addEventListener("click", refreshToken);
document.getElementById("logoutButton").addEventListener("click", logout);

renderState();

async function login() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    await sendRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
    }, true);
}

async function getMessage() {
    if (!state.accessToken) {
        showStatus("Waiting", "You need to log in first.", "No access token is available yet.");
        return;
    }

    await sendRequest("/message", {
        method: "GET",
        headers: {
            Authorization: `Bearer ${state.accessToken}`
        }
    });
}

async function refreshToken() {
    if (!state.refreshToken) {
        showStatus("Waiting", "You need to log in first.", "No refresh token is available yet.");
        return;
    }

    await sendRequest("/auth/refresh", {
        method: "POST",
        body: JSON.stringify({ refreshToken: state.refreshToken })
    }, true);
}

async function logout() {
    if (!state.refreshToken) {
        showStatus("Waiting", "An active refresh token is required for logout.", "No refresh token is available yet.");
        return;
    }

    const success = await sendRequest("/auth/logout", {
        method: "POST",
        body: JSON.stringify({ refreshToken: state.refreshToken })
    });

    if (success) {
        clearTokens();
        showStatus("204", "Logout completed successfully.", "The refresh token has been revoked.");
    }
}

async function sendRequest(url, options, shouldStoreTokens = false) {
    const requestOptions = {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    };

    try {
        const response = await fetch(url, requestOptions);
        const text = await response.text();
        const parsedBody = parseBody(text);

        if (shouldStoreTokens && response.ok) {
            updateTokens(parsedBody);
        }

        showStatus(
            String(response.status),
            response.ok ? "Request completed successfully." : "Request was rejected.",
            formatBody(parsedBody)
        );

        return response.ok;
    } catch (error) {
        showStatus("Error", "The server could not be reached.", error.message);
        return false;
    }
}

function updateTokens(payload) {
    state.accessToken = payload.accessToken || "";
    state.refreshToken = payload.refreshToken || "";
    state.accessTokenExpiresAt = payload.accessTokenExpiresAt || "";
    state.refreshTokenExpiresAt = payload.refreshTokenExpiresAt || "";
    renderState();
}

function clearTokens() {
    state.accessToken = "";
    state.refreshToken = "";
    state.accessTokenExpiresAt = "";
    state.refreshTokenExpiresAt = "";
    renderState();
}

function renderState() {
    accessTokenField.value = state.accessToken;
    refreshTokenField.value = state.refreshToken;
    accessExpireField.textContent = formatDate(state.accessTokenExpiresAt);
    refreshExpireField.textContent = formatDate(state.refreshTokenExpiresAt);
}

function parseBody(text) {
    if (!text) {
        return { message: "Empty response" };
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return { message: text };
    }
}

function formatBody(body) {
    return JSON.stringify(body, null, 2);
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString("en-US");
}

function showStatus(code, text, body) {
    statusCode.textContent = code;
    statusText.textContent = text;
    responseBox.textContent = body;
}
