document.addEventListener("DOMContentLoaded", function () {
  const permissionButtons = document.querySelectorAll(".permission-button");

  permissionButtons.forEach((button) => {
    button.addEventListener("click", () => {
      toggleButton(button);
      updatePermission(button);
    });
  });
});

function updatePermission(button) {
  const table = document.getElementById("permission-user-table");
  const userRow = table.querySelector(".focus");
  const userId = userRow.getAttribute("value");

  const fileId = button.parentNode.getAttribute("value");
  const permission = button.textContent;

  const formData = new FormData();
  formData.append("userId", userId);
  formData.append("fileId", fileId);
  formData.append("permission", permission);

  fetch("/ehz/admin/update-permissions", {
    method: "POST",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data === true) {
        console.log("Password update succeeded.");
      } else {
        console.error("Password update failed.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

function toggleButton(button) {
  const buttons = button.parentNode.querySelectorAll(".permission-button");
  buttons.forEach((btn) => {
    if (btn === button) {
      btn.classList.add("clicked");
    } else {
      btn.classList.remove("clicked");
    }
  });
}
