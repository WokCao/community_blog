const menuBtn = document.getElementById("menu-btn");
const sidebar = document.getElementById("sidebar");
const closeBtn = document.getElementById("close-btn");

menuBtn.addEventListener("click", () => {
    sidebar.classList.remove("-translate-x-full");
});
closeBtn.addEventListener("click", () => {
    sidebar.classList.add("-translate-x-full");
});