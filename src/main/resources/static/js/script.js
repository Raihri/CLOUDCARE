document.addEventListener("DOMContentLoaded", () => {

  document.querySelectorAll("a").forEach(link => {
    link.addEventListener("click", e => e.stopPropagation());
  });

  let sidebar = document.querySelector(".sidebar");
  let sidebarBtn = document.querySelector(".bx-menu");

  if (sidebar && sidebar.classList.contains("close")) {
    sidebar.classList.remove("close");
  }

  if (sidebarBtn) {
    sidebarBtn.addEventListener("click", () => {
      sidebar.classList.toggle("close");
    });
  }
});