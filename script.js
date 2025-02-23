document.addEventListener("DOMContentLoaded", function () {
    console.log("JavaScript Loaded!");

    // Smooth scrolling for navigation links
    document.querySelectorAll(".nav-link").forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            const targetId = this.getAttribute("href").substring(1);
            const targetElement = document.getElementById(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({ behavior: "smooth" });
            }
        });
    });

    // Redirect to home when clicking on company name
    const companyName = document.getElementById("companyName");
    if (companyName) {
        companyName.addEventListener("click", function () {
            window.location.href = "index.html";
        });
    }

    // Fetch user progress from backend
    function fetchProgress() {
        fetch("http://localhost:8080/progress/status")
            .then(response => response.json())
            .then(data => {
                Object.keys(data).forEach(user => {
                    const progressBar = document.getElementById(`progress-${user}`);
                    if (progressBar) {
                        progressBar.style.width = data[user] + "%";
                        progressBar.innerText = data[user] + "%";
                    }
                });
            })
            .catch(error => console.error("Error fetching progress:", error));
    }

    // Update progress on button click
    const updateProgressBtn = document.getElementById("update-progress");
    if (updateProgressBtn) {
        updateProgressBtn.addEventListener("click", function () {
            const user = "John";  // You can dynamically get the user input later
            const percentage = document.getElementById("progressInput").value || 50;
            
            fetch(`http://localhost:8080/progress/update?user=${user}&percentage=${percentage}`, { method: "POST" })
                .then(response => response.text())
                .then(() => fetchProgress())
                .catch(error => console.error("Error updating progress:", error));
        });
    }

    // Responsive Menu Toggle
    const menuToggle = document.getElementById("menu-toggle");
    const navMenu = document.getElementById("nav-menu");

    if (menuToggle && navMenu) {
        menuToggle.addEventListener("click", function () {
            navMenu.classList.toggle("hidden");
            navMenu.classList.toggle("flex");
        });
    }

    // Display progress data on home page
    function displayProgress() {
        fetch("http://localhost:8080/progress/status")
            .then(response => response.json())
            .then(data => {
                const progressDisplay = document.getElementById("progressDisplay");
                if (progressDisplay) {
                    progressDisplay.innerText = JSON.stringify(data, null, 2);
                }
            })
            .catch(error => console.error("Error fetching progress:", error));
    }

    // Initial fetch calls
    fetchProgress();
    displayProgress();
});
