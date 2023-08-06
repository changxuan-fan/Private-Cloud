function displayDescription() {
  const descriptionValue = this.dataset.description;
  const descriptionUrl = this.dataset.descriptionUrl;

  const formElement = document.querySelector(".form-description");
  const inputElement = document.querySelector(".input-description");

  // Set attributes for form and input elements
  formElement.setAttribute("action", descriptionUrl);
  inputElement.textContent = descriptionValue;

  const divDescription = document.getElementById("div-description");
  if (divDescription) {
    divDescription.style.display = "block";
  } else {
    console.error("Element with ID 'div-description' not found.");
  }
}

function displayDownload() {
  const downloadUrl = this.value;

  const formElement = document.querySelector(".form-download");
  formElement.setAttribute("action", downloadUrl);

  formElement.submit();
}

function displayAuthor() {
  const authorValue = this.dataset.author;
  const authorUrl = this.dataset.authorUrl;

  const formElement = document.querySelector(".form-author");
  const inputElement = document.querySelector(".input-author");

  // Set attributes for form and input elements
  formElement.setAttribute("action", authorUrl);
  inputElement.value = authorValue;

  const divAuthor = document.getElementById("div-author");
  if (divAuthor) {
    divAuthor.style.display = "block";
  } else {
    console.error("Element with ID 'div-author' not found.");
  }
}

function displayDelete() {
  const deleteUrl = this.value;

  const formElement = document.querySelector(".form-delete");

  formElement.setAttribute("action", deleteUrl);

  const divDelete = document.getElementById("div-delete");
  if (divDelete) {
    divDelete.style.display = "block";
  } else {
    console.error("Element with ID 'div-description' not found.");
  }
}

// Function to handle the Description Form submission
function handleDescriptionSubmit(event) {
  event.preventDefault(); // Prevent the default form submission and page reload

  const form = event.target; // Get the form element that was submitted
  const formData = new FormData(form); // Create a FormData object to collect form data

  // Get the value of the "action" attribute
  const actionUrl = form.getAttribute("action");

  // AJAX fetch: Update the Description
  fetch(actionUrl, {
    method: "POST",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data === true) {
        location.reload(); // Reload the page
      } else {
        console.error("Description update failed.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

// Add an event listener to the description form submit event
const formDescription = document.getElementById("form-description");
formDescription.addEventListener("submit", handleDescriptionSubmit);

// Function to handle the Author Form submission
function handleAuthorSubmit(event) {
  event.preventDefault();

  const form = event.target;
  const formData = new FormData(form);

  // Get the value of the "action" attribute
  const actionUrl = form.getAttribute("action");

  // AJAX fetch: Update the Description
  fetch(actionUrl, {
    method: "POST",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data === true) {
        location.reload(); // Reload the page
      } else {
        console.error("Author update failed.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

// Add an event listener to the description form submit event
const formAuthor = document.getElementById("form-author");
formAuthor.addEventListener("submit", handleAuthorSubmit);

// Function to handle the Delete Form submission
function handleDeleteSubmit(event) {
  event.preventDefault();

  const form = event.target;

  // Get the value of the "action" attribute
  const actionUrl = form.getAttribute("action");

  // AJAX fetch: Update the Description
  fetch(actionUrl, {
    method: "Get",
  })
    .then((response) => response.json())
    .then((data) => {
      if (data === true) {
        location.reload(); // Reload the page
      } else {
        console.error("Delete failed.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

// Add an event listener to the description form submit event
const formDelete = document.getElementById("form-delete");
formDelete.addEventListener("submit", handleDeleteSubmit);
