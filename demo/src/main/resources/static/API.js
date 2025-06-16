const BASE_URL = "http://localhost:8080/api/v1";

// --- AUTH CONTROLLER ---
export async function authRegister(data) {
  try {
    const response = await fetch(`${BASE_URL}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    const text = await response.text(); // get raw response
    const result = text ? JSON.parse(text) : {}; // safely parse JSON

    if (!response.ok) {
      throw new Error(result.message || "Registration failed");
    }

    return result;
  } catch (error) {
    console.error("Registration error:", error);
    throw error;
  }
}


export async function authLogin(data) {
    const response = await fetch(`${BASE_URL}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: 'include',
        body: JSON.stringify(data),
    });
    return response.json();
}

export async function checkHealth() {
    const response = await fetch(`${BASE_URL}/auth/health`);
    return response.text();
}

// --- USER CONTROLLER ---
export async function userSignup(data) {
    const response = await fetch(`${BASE_URL}/users/signup`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: 'include',
        body: JSON.stringify(data),
    });
    return response.json();
}

export async function userLogin(data) {
    const response = await fetch(`${BASE_URL}/users/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: 'include',
        body: JSON.stringify(data),
    });
    return response.json();
}