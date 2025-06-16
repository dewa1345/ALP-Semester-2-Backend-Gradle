// Import functions from API.js
import { authRegister, authLogin } from './API.js';

// Slide Panel Toggle
document.getElementById('signUp').addEventListener('click', () => {
  document.getElementById('container').classList.add("right-panel-active");
});
document.getElementById('signIn').addEventListener('click', () => {
  document.getElementById('container').classList.remove("right-panel-active");
});

// Toggle Password Visibility
function togglePassword(id) {
  const input = document.getElementById(id);
  input.type = input.type === "password" ? "text" : "password";
}

// Background Slideshow
const images = ["evo1.jpg", "evo2.jpg", "evo3.jpg", "evo4.jpg"];
let leftIndex = 0;
let rightIndex = 2;

const bgLeft = document.getElementById('bg-left');
const bgRight = document.getElementById('bg-right');

function updateBackgrounds() {
  bgLeft.classList.remove('active');
  setTimeout(() => {
    bgLeft.style.backgroundImage = `url(${images[leftIndex]})`;
    bgLeft.classList.add('active');
  }, 100);

  bgRight.classList.remove('active');
  setTimeout(() => {
    bgRight.style.backgroundImage = `url(${images[rightIndex]})`;
    bgRight.classList.add('active');
  }, 100);

  leftIndex = (leftIndex + 1) % images.length;
  rightIndex = (rightIndex + 1) % images.length;
}

updateBackgrounds();
setInterval(updateBackgrounds, 5000);

// Login Logic
document.getElementById("login-btn").addEventListener("click", function () {
  const email = document.getElementById("login-name").value.trim();
  const password = document.getElementById("login-password").value.trim();

  if (!email || !password) {
    alert("Semua input wajib diisi!");
    return;
  }

  authLogin({ email, password })
    .then((data) => {
      if (data.success) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("username", data.name);
        localStorage.setItem("email", data.email);
        window.location.href = "profile.html";
      } else {
        alert("Login gagal: " + data.message);
      }
    })
    .catch((error) => {
      console.error("Login error:", error);
      alert("Terjadi kesalahan saat login.");
    });
});

// Register Logic
document.getElementById("register-btn").addEventListener("click", function () {
  const name = document.getElementById("register-name").value.trim();
  const email = document.getElementById("register-email").value.trim();
  const password = document.getElementById("register-password").value.trim();

  if (!name || !email || !password) {
    alert("Semua input wajib diisi!");
    return;
  }

  authRegister({ name, email, password })
    .then((data) => {
      if (data.success) {
        alert("Registrasi berhasil! Silakan login.");
        document.getElementById('signIn').click();
      } else {
        alert("Registrasi gagal: " + data.message);
      }
    })
    .catch((error) => {
      console.error("Register error:", error);
      alert("Terjadi kesalahan saat registrasi.");
    });
});
