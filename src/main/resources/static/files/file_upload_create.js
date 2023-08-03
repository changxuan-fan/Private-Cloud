// Check if there are conflicting File names
const fileInput = document.getElementById("files");

fileInput.addEventListener("change", checkDuplicates);

function checkDuplicates(event) {
  const files = event.target.files;

  // Perform AJAX request to the server to check for name duplicates
  const formData = new FormData();
  for (let i = 0; i < files.length; i++) {
    formData.append("files", files[i].name); // Append files to the key 'file'
  }

  // AJAX fetch: check duplicate file names
  fetch(`/ehz/files/${uuid}/check-duplicates`, {
    method: "POST",
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data === true) {
        // If there is a conflict
        document.getElementById("div-upload").style.display = "block";
      } else {
        // If there are no conflicts, submit the form directly.
        document.getElementById("fileUploadForm").submit();
      }
    })
    .catch((error) => {
      // Handle error during AJAX request or server-side check.
      console.error("Error:", error);
      // You can provide appropriate feedback to the user here.
    });
}

// Check if there are conflicting Folder names
function checkDuplicateFolder() {
  const filename = document.getElementById("filename").value;

  // Perform AJAX request to the server to check for name duplicates
  const formData = new FormData();
  formData.append("files", filename);

  // AJAX fetch: check duplicate file names
  fetch(`/ehz/files/${uuid}/check-duplicates`, {
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
