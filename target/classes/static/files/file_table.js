const response = fileList;

const tableContent = document.getElementById("table-content");

// Select all th elements that have a button child except for the last one
const tableButtons = document.querySelectorAll("th button:not(#Options)");

// Create a row of the file table
const createRow = (obj) => {
    // Create Filename Cell
    const row = document.createElement("tr");
    const cellFilename = document.createElement("td");

    const {filename, fileType, isDirectory, uuid, description, permission} = obj;

    const divFilename = document.createElement("div");
    divFilename.classList.add("folder-file");

    const imgTag = document.createElement("img");
    imgTag.classList.add("svg-file");

    const fileTypesMap = {
        "PowerPoint": "/ppt.svg",
        "Word": "/word.svg",
        "Excel": "/excel.svg",
        "PDF": "/pdf.svg",
        "Image": "/image.svg",
        "Video": "/video.svg",
        "Audio": "/audio.svg",
        "Other": "/other_file.svg",
    };

    const src = isDirectory === "true" ? "/folder.svg" : fileTypesMap[fileType] || "/other_file.svg";
    const alt = isDirectory === "true" ? "Directory" : fileType;

    imgTag.setAttribute("src", src);
    imgTag.setAttribute("alt", alt);

    const fileLink = document.createElement("a");
    fileLink.textContent = filename;
    fileLink.setAttribute("href", `/ehz/files/${uuid}`);
    fileLink.classList.add("file-link");

    divFilename.appendChild(imgTag);
    divFilename.appendChild(fileLink);
    cellFilename.appendChild(divFilename);

    row.appendChild(cellFilename);

    // Create Other Properties' Cell
    const properties = ['filePath', 'uploadDate', 'uploadUser', 'fileSize'];

    for (const prop of properties) {
        const cell = document.createElement("td");
        cell.textContent = obj[prop];
        row.appendChild(cell);
    }

    // Create Option Cell
    const cellOptions = document.createElement("td");

    const divOptions = document.createElement("div");
    divOptions.classList.add("file-options");

    const imgTagDescription = document.createElement("img");
    imgTagDescription.classList.add("svg-options");

    imgTagDescription.setAttribute("src", "/description.svg");
    imgTagDescription.setAttribute("alt", "Description");

    const descriptionButton = document.createElement("button");
    descriptionButton.appendChild(imgTagDescription);
    descriptionButton.dataset.description = description;
    descriptionButton.dataset.descriptionUrl = `/ehz/files/${uuid}/description`;
    descriptionButton.onclick = displayDescription; // Assigning the description function to the onclick event
    descriptionButton.classList.add("button-options");

    if (permission === "None") {
        descriptionButton.hidden = true;
    }

    // Append the First Description Button
    divOptions.appendChild(descriptionButton);


    const imgTagDownload = document.createElement("img");
    imgTagDownload.classList.add("svg-options");

    imgTagDownload.setAttribute("src", "/download.svg");
    imgTagDownload.setAttribute("alt", "Download");

    const downloadButton = document.createElement("button");
    downloadButton.appendChild(imgTagDownload);
    downloadButton.value = `/ehz/files/${uuid}/download`;
    downloadButton.onclick = displayDownload; // Assigning the download function to the onclick event
    downloadButton.classList.add("button-options");

    if (permission !== "Download" && permission !== "Modify") {
        downloadButton.hidden = true;
    }

    // Append the First Description Button
    divOptions.appendChild(downloadButton);


    const imgTagDelete = document.createElement("img");
    imgTagDelete.classList.add("svg-options");

    imgTagDelete.setAttribute("src", "/delete.svg");
    imgTagDelete.setAttribute("alt", "Delete");

    const deleteButton = document.createElement("button");
    deleteButton.appendChild(imgTagDelete);
    deleteButton.value = `/ehz/files/${uuid}/delete`;
    deleteButton.onclick = displayDelete; // Assigning the delete function to the onclick event
    deleteButton.classList.add("button-options");

    if (permission !== "Modify") {
        deleteButton.hidden = true;
    }

    // Append the delete Button
    divOptions.appendChild(deleteButton);

    cellOptions.appendChild(divOptions)


    row.appendChild(cellOptions);

    return row;
};

// Create all rows of the file table
const getTableContent = (data) => {
    data.map((obj) => {
        const row = createRow(obj);
        tableContent.appendChild(row);
    });
};

// Sort the rows in the file table
const sortData = (data, param, direction) => {
    const sortedData = [...data].sort((a, b) => {
        const isADirectory = a.isDirectory === "true";
        const isBDirectory = b.isDirectory === "true";

        // Sorting directories before files
        if (isADirectory && !isBDirectory) {
            return direction === "asc" ? -1 : 1;
        }
        if (!isADirectory && isBDirectory) {
            return direction === "asc" ? 1 : -1;
        }

        // Sorting based on the specified parameter
        return direction === "asc" ? a[param].localeCompare(b[param]) : b[param].localeCompare(a[param]);
    });

    // Empty the table, then display the sortedData
    tableContent.innerHTML = '';
    getTableContent(sortedData);
};

// Reset the Sorting Button
const resetButtons = (event) => {
    tableButtons.forEach((button) => {
        if (button !== event.target) {
            button.removeAttribute("data-dir");
        }
    });
};

// Handle table displaying and sorting
window.addEventListener("load", () => {
    // Sort the data when the window is loading
    sortData(response, "filename", "asc");

    tableButtons.forEach((button) => {
        button.addEventListener("click", (e) => {
            resetButtons(e);

            // Sort the data based on "asc" or "desc, default first click is "desc"
            const isAsc = e.target.getAttribute("data-dir") === "asc";
            sortData(response, e.target.id, isAsc ? "asc" : "desc");
            e.target.setAttribute("data-dir", isAsc ? "desc" : "asc");
        });
    });
});

// Handle single click and double click event
document.addEventListener('DOMContentLoaded', () => {
    const tableContent = document.querySelector('.table-content');

    const addFocusToRow = (row) => {
        const tableRows = tableContent.querySelectorAll('tr');
        tableRows.forEach(row => row.classList.remove('focus'));
        row.classList.add('focus');
    };

    const openLinkInSameTab = (row) => {
        const firstTd = row.querySelector('td');
        const anchorElement = firstTd.querySelector('a');
        if (anchorElement && anchorElement.hasAttribute('href')) {
            window.location.href = anchorElement.getAttribute('href');
        }
    };

    // Single Click leads to focus: turning blue
    const handleClick = (event) => {
        const targetRow = event.target.closest('tr');
        if (targetRow && tableContent.contains(targetRow)) {
            addFocusToRow(targetRow);
        }
    };

    // Double Click leads to opening the file
    const handleDoubleClick = (event) => {
        const targetRow = event.target.closest('tr');
        if (targetRow) {
            openLinkInSameTab(targetRow);
        }
    };

    // Losing focus after clicking outside of the table
    const handleDocumentClick = (event) => {
        const target = event.target;
        if (!tableContent.contains(target)) {
            const tableRows = tableContent.querySelectorAll('tr');
            tableRows.forEach(row => row.classList.remove('focus'));
        }
    };

    tableContent.addEventListener('click', handleClick);
    tableContent.addEventListener('dblclick', handleDoubleClick);
    document.addEventListener('click', handleDocumentClick);
});



