function handleCreateSubmit(event) {
  event.preventDefault(); // Prevent the default form submission and page reload

  const usernameInput = document.getElementById("username");
  const username = usernameInput.value.trim();

  if (username.length < 4) {
    document.getElementById("label-create-hidden").style.visibility = "visible";
  } else {
    // Perform AJAX request to the server to check for name duplicates
    const formData = new FormData();
    formData.set("username", username);

    // AJAX fetch: check duplicate usernames
    fetch("/ehz/admin/username-check", {
      method: "POST",
      body: formData,
    })
      .then((response) => response.json())
      .then((data) => {
        if (data === true) {
          // If there is a conflict
          document.getElementById("label-create-hidden").style.visibility =
            "visible";
        } else {
          // If there are no conflicts, submit the form directly.
          document.getElementById("form-create").submit();
        }
      })
      .catch((error) => {
        // Handle error during AJAX request or server-side check.
        console.error("Error:", error);
        // You can provide appropriate feedback to the user here.
      });
  }
}

// Add an event listener to the form's submit event
const formCreate = document.getElementById("form-create");
formCreate.addEventListener("submit", handleCreateSubmit);
