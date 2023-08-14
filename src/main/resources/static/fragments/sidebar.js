// Define the function to highlight active links
function highlightActiveLinks() {
  // Get the current pathname
  const currentPath = window.location.pathname;

  // Find the corresponding <a> elements based on the current pathname
  const links = document.querySelector(".sidebar").querySelectorAll("a");

  // Loop through the links and check if the href starts with the currentPath
  for (let i = 0; i < links.length; i++) {
    if (currentPath.startsWith(links[i].getAttribute("href"))) {
      links[i].classList.add("active");
      break; // Exit the loop after the first matching link is found
    }
  }
}

// Execute the function when the DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  highlightActiveLinks();
});

// Define the function to fetch root size and update the element
async function fetchRootSizeAndUpdate() {
  try {
    const response = await fetch("/getRootSize");
    const data = await response.text();
    if (data !== null) {
      document.getElementById("rootSize").textContent = data;
    }
  } catch (error) {
    console.error("Error:", error);
  }
}

// Execute the function when the DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  fetchRootSizeAndUpdate();
});
