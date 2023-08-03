const response = fileList;

const tableContent = document.getElementById("table-content");

// Select all th elements that have a button child except for the last one
const tableButtons = document.querySelectorAll("th button:not(#Options)");

// Create a row of the file table
const createRow = (obj) => {
  // Create Filename Cell
  const row = document.createElement("tr");
  const cellFilename = document.createElement("td");

  const {
    filename,
    fileType,
    isDirectory,
    uuid,
    description,
    permission,
    author,
  } = obj;

  const divFilename = document.createElement("div");
  divFilename.classList.add("folder-file");

  const imgTag = document.createElement("img");
  imgTag.classList.add("svg-file");

  const fileTypesMap = {
    PowerPoint: "/ppt.svg",
    Word: "/word.svg",
    Excel: "/excel.svg",
    PDF: "/pdf.svg",
    Image: "/image.svg",
    Video: "/video.svg",
    Audio: "/audio.svg",
    Other: "/other_file.svg",
  };

  const src =
    isDirectory === "true"
      ? "/folder.svg"
      : fileTypesMap[fileType] || "/other_file.svg";
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
  const properties = ["description", "uploadDate", "author", "fileSize"];

  for (const prop of properties) {
    const cell = document.createElement("td");
    cell.textContent = obj[prop];
    row.appendChild(cell);
  }

  // Create Option Cell
  const cellOptions = document.createElement("td");

  const divOptions = document.createElement("div");
  divOptions.classList.add("file-options");

  // Append the First Description Button
  const imgTagDescription = document.createElement("img");
  imgTagDescription.classList.add("svg-options");

  imgTagDescription.setAttribute("src", "/description.svg");
  imgTagDescription.setAttribute("alt", "Description");

  const descriptionButton = document.createElement("button");
  descriptionButton.appendChild(imgTagDescription);
  descriptionButton.dataset.description = description;
  descriptionButton.dataset.descriptionUrl = `/ehz/files/${uuid}/description`;
  debugger;
  descriptionButton.onclick = displayDescription; // Assigning the description function to the onclick event
  descriptionButton.classList.add("button-options");

  if (permission === "NONE") {
    descriptionButton.hidden = true;
  }

  divOptions.appendChild(descriptionButton);

  // Append the Author Button
  const imgTagAuthor = document.createElement("img");
  imgTagAuthor.classList.add("svg-options");

  imgTagAuthor.setAttribute("src", "/author.svg");
  imgTagAuthor.setAttribute("alt", "Author");

  const authorButton = document.createElement("button");
  authorButton.appendChild(imgTagAuthor);
  authorButton.dataset.author = author;
  authorButton.dataset.authorUrl = `/ehz/files/${uuid}/author`;
  authorButton.onclick = displayAuthor; // Assigning the description function to the onclick event
  authorButton.classList.add("button-options");

  if (permission === "NONE") {
    authorButton.hidden = true;
  }

  divOptions.appendChild(authorButton);

  // Append the Download Button
  const imgTagDownload = document.createElement("img");
  imgTagDownload.classList.add("svg-options");

  imgTagDownload.setAttribute("src", "/download.svg");
  imgTagDownload.setAttribute("alt", "Download");

  const downloadButton = document.createElement("button");
  downloadButton.appendChild(imgTagDownload);
  downloadButton.value = `/ehz/files/${uuid}/download`;
  downloadButton.onclick = displayDownload; // Assigning the download function to the onclick event
  downloadButton.classList.add("button-options");

  if (permission !== "DOWNLOAD" && permission !== "MODIFY") {
    downloadButton.hidden = true;
  }

  divOptions.appendChild(downloadButton);

  // Append the Delete Button
  const imgTagDelete = document.createElement("img");
  imgTagDelete.classList.add("svg-options");

  imgTagDelete.setAttribute("src", "/delete.svg");
  imgTagDelete.setAttribute("alt", "Delete");

  const deleteButton = document.createElement("button");
  deleteButton.appendChild(imgTagDelete);
  deleteButton.value = `/ehz/files/${uuid}/delete`;
  deleteButton.onclick = displayDelete; // Assigning the delete function to the onclick event
  deleteButton.classList.add("button-options");

  if (permission !== "MODIFY") {
    deleteButton.hidden = true;
  }

  divOptions.appendChild(deleteButton);

  cellOptions.appendChild(divOptions);

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
    return direction === "asc"
      ? a[param].localeCompare(b[param])
      : b[param].localeCompare(a[param]);
  });

  // Empty the table, then display the sortedData
  tableContent.innerHTML = "";
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

function displayTable() {
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
}

// Handle table displaying and sorting
document.addEventListener("DOMContentLoaded", displayTable);

// Handle single click and double click event
document.addEventListener("DOMContentLoaded", () => {
  const tableContent = document.querySelector(".table-content");

  const addFocusToRow = (row) => {
    const tableRows = tableContent.querySelectorAll("tr");
    tableRows.forEach((row) => row.classList.remove("focus"));
    row.classList.add("focus");
  };

  const openLinkInSameTab = (row) => {
    const firstTd = row.querySelector("td");
    const anchorElement = firstTd.querySelector("a");
    if (anchorElement && anchorElement.hasAttribute("href")) {
      window.location.href = anchorElement.getAttribute("href");
    }
  };

  // Single Click leads to focus: turning blue
  const handleClick = (event) => {
    const targetRow = event.target.closest("tr");
    if (targetRow && tableContent.contains(targetRow)) {
      addFocusToRow(targetRow);
    }
  };

  // Double Click leads to opening the file
  const handleDoubleClick = (event) => {
    const targetRow = event.target.closest("tr");
    if (targetRow) {
      openLinkInSameTab(targetRow);
    }
  };

  // Losing focus after clicking outside the table
  const handleDocumentClick = (event) => {
    const target = event.target;
    if (!tableContent.contains(target)) {
      const tableRows = tableContent.querySelectorAll("tr");
      tableRows.forEach((row) => row.classList.remove("focus"));
    }
  };

  tableContent.addEventListener("click", handleClick);
  tableContent.addEventListener("dblclick", handleDoubleClick);
  document.addEventListener("click", handleDocumentClick);
});

// Check if the query attribute is not null
if (query !== null) {
  // Highlight Search Result
  function highlightSearch() {
    const table = document.getElementById("table-content");
    const rows = table.getElementsByTagName("tr");

    for (let row of rows) {
      const cells = row.getElementsByTagName("td");
      for (let i = 0; i < cells.length - 1; i++) {
        const cell = cells[i];
        const cellText = cell.innerHTML;

        // only highlight the specific query instead of the whole cell
        if (cellText.includes(query)) {
          const regex = new RegExp(query, "gi");
          const highlightedText = cellText.replace(
            regex,
            (match) => `<mark>${match}</mark>`,
          );
          cell.innerHTML = highlightedText;
        }
      }
    }
  }

  document.addEventListener("DOMContentLoaded", highlightSearch);
}
