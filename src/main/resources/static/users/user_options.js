function displayPassword() {
  const passwordValue = this.dataset.password;
  const passwordUrl = this.dataset.passwordUrl;

  const formElement = document.querySelector(".form-password");
  const inputElement = document.querySelector(".input-password");

  // Set attributes for form and input elements
  formElement.setAttribute("action", passwordUrl);
  inputElement.value = passwordValue;

  const divPassword = document.getElementById("div-password");
  if (divPassword) {
    divPassword.style.display = "block";
  } else {
    console.error("Element with ID 'div-password' not found.");
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
    console.error("Element with ID 'div-delete' not found.");
  }
}

function displayEnabled() {
  const enabledUrl = this.value;

  const formElement = document.querySelector(".form-enabled");

  formElement.setAttribute("action", enabledUrl);

  const divEnabled = document.getElementById("div-enabled");
  if (divEnabled) {
    divEnabled.style.display = "block";
  } else {
    console.error("Element with ID 'div-enabled' not found.");
  }
}

// Function to handle the Password Form submission
function handlePasswordSubmit(event) {
  event.preventDefault();

  const form = event.target;
  const formData = new FormData(form);

  // Get the value of the "action" attribute
  const actionUrl = form.getAttribute("action");

  // AJAX fetch: Update the password
  fetch(actionUrl, {
    method: "POST",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data === true) {
        location.reload(); // Reload the page
      } else {
        console.error("Password update failed.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

// Add an event listener to the password form submit event
const formPassword = document.getElementById("form-password");
formPassword.addEventListener("submit", handlePasswordSubmit);

// Function to handle the Enabled Form submission
function handleEnabledSubmit(event) {
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
        console.error("Enabled failed.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

// Add an event listener to the enabled form submit event
const formEnabled = document.getElementById("form-enabled");
formEnabled.addEventListener("submit", handleEnabledSubmit);

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

// Add an event listener to the delete form submit event
const formDelete = document.getElementById("form-delete");
formDelete.addEventListener("submit", handleDeleteSubmit);
