// Get the current pathname
const currentPath = window.location.pathname;

// Find the corresponding <a> element based on the current pathname
const links = document.querySelector(".sidebar").querySelectorAll("a");

// Loop through the links and check if the href starts with the currentPath
for (let i = 0; i < links.length; i++) {
  if (currentPath.startsWith(links[i].getAttribute("href"))) {
    links[i].classList.add("active");
    break; // Exit the loop after the first matching link is found
  }
}
