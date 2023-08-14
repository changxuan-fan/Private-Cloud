function displayMessage() {
  // Get the message element
  const messageElement = document.getElementById("message");

  // Check if the message exists and is not empty
  if (messageElement && messageElement.textContent !== "") {
    // Display the message for 3 seconds
    messageElement.style.display = "block";
    setTimeout(function () {
      messageElement.style.display = "none";
    }, 3000);
  }
}

document.addEventListener("DOMContentLoaded", displayMessage);
