const uuidOriginal = uuidString;

function displayDescription() {
    const descriptionValue = this.dataset.description;
    const descriptionUrl = this.dataset.descriptionUrl;

    const formElement = document.querySelector(".form-description");
    const inputElement = document.querySelector(".input-description");
    const hiddenElement = document.querySelector(".input-description-hidden");

    // Set attributes for form and input elements
    formElement.setAttribute("action", descriptionUrl);
    inputElement.textContent = descriptionValue;
    hiddenElement.setAttribute("value", uuidOriginal);

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

function displayDelete() {
    const deleteUrl = this.value;

    const formElement = document.querySelector(".form-delete");
    const inputElement = document.querySelector(".input-delete-hidden");

    formElement.setAttribute("action", deleteUrl);
    inputElement.setAttribute("value", uuidOriginal);

    const divDelete = document.getElementById("div-delete");
    if (divDelete) {
        divDelete.style.display = "block";
    } else {
        console.error("Element with ID 'div-description' not found.");
    }
}
