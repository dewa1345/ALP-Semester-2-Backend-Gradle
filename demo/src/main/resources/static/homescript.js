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
