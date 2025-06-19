// Carousel
let slideIndex = 0;
const slides = document.querySelector(".slides");
const dots = document.querySelectorAll(".dot");

function showSlide(index) {
  slideIndex = index;
  slides.style.transform = `translateX(-${index * 100}%)`;
  dots.forEach(dot => dot.classList.remove("active"));
  dots[index].classList.add("active");
}

function autoSlide() {
  slideIndex = (slideIndex + 1) % dots.length;
  showSlide(slideIndex);
}

dots.forEach((dot, index) => {
  dot.addEventListener("click", () => showSlide(index));
});

setInterval(autoSlide, 5000);

// Chatbot Elements
const openChatBtn = document.getElementById('openChat');
const closeChatBtn = document.getElementById('closeChat');
const chatPopup = document.getElementById('chatPopup');
const chatMessages = document.getElementById('chatMessages');
const chatForm = document.getElementById('chatForm');
const chatInput = document.getElementById('chatInput');
const username = localStorage.getItem('username') || 'Pengguna';

let lastTopic = '';

openChatBtn.addEventListener('click', () => {
  chatPopup.style.display = 'flex';
  chatInput.focus();
});

closeChatBtn.addEventListener('click', () => {
  chatPopup.style.display = 'none';
});

function getBotReply(userMsg) {
  const msg = userMsg.toLowerCase().trim();
  const greetings = ['hai', 'halo', 'hi', 'selamat pagi', 'selamat siang', 'selamat sore', 'selamat malam'];

  if (msg.includes('terima kasih') || msg.includes('makasih')) {
    return `Terima kasih kembali, ${username}!`;
  }

  if (msg.includes('gambar') && msg.includes('organik')) {
    return `Berikut contoh gambar sampah <b>Organik</b>:<br>
    <img src="Gambar/organik.jpeg" alt="Sampah Organik"><br>
    Misalnya: daun, sisa makanan, buah.`;
  }

  if (msg.includes('gambar') && (msg.includes('anorganik') || msg.includes('non organik'))) {
    return `Berikut contoh gambar sampah <b>Non Organik</b>:<br>
    <img src="Gambar/anorganik.jpeg" alt="Sampah Non Organik"><br>
    Misalnya: botol plastik, kaleng, kaca.`;
  }

  if (greetings.some(greet => msg.includes(greet))) {
    return `Halo, ${username}! Ada yang bisa aku bantu tentang sampah atau daur ulang?`;
  }

  if (msg.includes('manfaat') || msg.includes('apa fungsinya') || msg === 'apa itu' || msg.includes('guna')) {
    switch (lastTopic) {
      case 'botol': return 'Botol bisa dimanfaatkan sebagai pot tanaman, tempat pensil, hiasan, atau wadah ulang.';
      case 'plastik': return 'Plastik dapat didaur ulang menjadi tas, ubin bangunan, dan kerajinan lainnya.';
      case 'daun': return 'Daun bisa dijadikan pupuk kompos yang bermanfaat untuk tanaman.';
      case 'kaleng': return 'Kaleng bisa digunakan untuk membuat kerajinan logam seperti vas bunga atau tempat pensil.';
      case 'kantong': return 'Kantong plastik bisa didaur ulang menjadi tas belanja atau barang kerajinan.';
      default: return 'Manfaat dari apa ya, ' + username + '? Coba sebutkan dulu nama sampahnya.';
    }
  }

  if (msg.includes('botol')) { lastTopic = 'botol'; return 'Botol termasuk sampah Non Organik. Biasanya berasal dari plastik atau kaca.'; }
  if (msg.includes('kantong') || msg.includes('plastik')) { lastTopic = 'plastik'; return 'Kantong plastik adalah sampah Non Organik. Tidak mudah terurai di alam.'; }
  if (msg.includes('daun') || msg.includes('sayur')) { lastTopic = 'daun'; return 'Daun dan sayur termasuk sampah Organik. Bisa terurai secara alami.'; }
  if (msg.includes('kaleng')) { lastTopic = 'kaleng'; return 'Kaleng termasuk sampah Non Organik yang bisa didaur ulang.'; }
  if (msg.includes('sampah organik')) { lastTopic = ''; return 'Contoh sampah organik: sisa makanan, daun, buah, kulit telur. Bisa dijadikan kompos.'; }
  if (msg.includes('sampah non organik') || msg.includes('sampah anorganik')) { lastTopic = ''; return 'Contoh sampah non organik: botol plastik, kaca, kaleng, styrofoam, plastik. Bisa didaur ulang.'; }
  if (msg.includes('daur ulang') || msg.includes('recycle')) { lastTopic = ''; return 'Daur ulang adalah proses mengubah sampah menjadi barang baru yang berguna.'; }
  if (msg.includes('sampah')) { lastTopic = ''; return 'Sampah terbagi menjadi organik (mudah terurai) dan non organik (sulit terurai). Yuk pilah dari rumah, ' + username + '!'; }

  return 'Maaf, saya belum mengerti maksudmu, ' + username + '. Coba gunakan kata seperti "botol", "plastik", atau "apa manfaatnya?"';
}

chatForm.addEventListener('submit', (e) => {
  e.preventDefault();
  const userText = chatInput.value.trim();
  if (!userText) return;

  const userMsgDiv = document.createElement('div');
  userMsgDiv.classList.add('message', 'user-message');
  userMsgDiv.textContent = userText;
  chatMessages.appendChild(userMsgDiv);

  chatInput.value = '';
  chatMessages.scrollTop = chatMessages.scrollHeight;

  setTimeout(() => {
    const botReply = getBotReply(userText);
    const botMsgDiv = document.createElement('div');
    botMsgDiv.classList.add('message', 'bot-message');
    botMsgDiv.innerHTML = botReply;
    chatMessages.appendChild(botMsgDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }, 700);
});

// Dot sync for slide animation
let currentSlide = 0;
const totalSlides = dots.length;

setInterval(() => {
  dots[currentSlide].classList.remove('active');
  currentSlide = (currentSlide + 1) % totalSlides;
  dots[currentSlide].classList.add('active');
}, 6667);

// Like button effect
document.querySelectorAll('.like-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    btn.classList.toggle('liked');
  });
});
