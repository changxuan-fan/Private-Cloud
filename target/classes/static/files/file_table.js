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
    uploadDate,
    fileSize,
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
  const properties = [description, uploadDate, author, fileSize];

  for (const prop of properties) {
    const cell = document.createElement("td");
    cell.textContent = prop;
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

  const openLinkInNewTab = (row) => {
    const firstTd = row.querySelector("td");
    const anchorElement = firstTd.querySelector("a");
    if (anchorElement && anchorElement.hasAttribute("href")) {
      const url = anchorElement.getAttribute("href");
      window.open(url, "_blank");
    }
  };

  // Single Click leads to focus: turning blue
  const handleClick = (event) => {
    const targetRow = event.target.closest("tr");
    if (targetRow && tableContent.contains(targetRow)) {
      // Add focus to the row
      addFocusToRow(targetRow);
    }
  };

  // Double Click leads to opening the file
  const handleDoubleClick = (event) => {
    const targetRow = event.target.closest("tr");
    if (targetRow) {
      // Find the <img> element within the clicked row and get its "alt" attribute
      const altAttributeValue = targetRow
        .querySelector("img")
        .getAttribute("alt");

      // Check if the "alt" attribute value indicates a directory
      if (altAttributeValue === "Directory") {
        openLinkInSameTab(targetRow); // If it's a directory, open the link in the same tab
      } else if (
        altAttributeValue === "Video" ||
        altAttributeValue === "Audio" ||
        altAttributeValue === "Other"
      ) {
        // Do nothing
      } else {
        openLinkInNewTab(targetRow); // If it's a file, open the link in a new tab
      }
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

    //  delete duplicate strings and also remove substrings from the list
    const uniqueQueryList = removeDuplicatesAndSubstrings(query);

    for (let row of rows) {
      const cells = row.getElementsByTagName("td");
      // Highlight the first column
      const firstCell = row.getElementsByClassName("file-link")[0];

      let cellText = firstCell.innerHTML; // Extract the text content and remove leading/trailing whitespace
      const regexPattern = new RegExp(uniqueQueryList.join("|"), "gi"); // Combine all queries in uniqueQueryList into a single regex pattern

      cellText = cellText.replace(
        // Replace all occurrences of the queries with <mark> tags
        regexPattern,
        (match) => `<mark>${match}</mark>`,
      );
      firstCell.innerHTML = cellText;

      // Highlight cells in columns except the first and last rows
      for (let i = 1; i < cells.length - 1; i++) {
        const cell = cells[i];
        let cellText = cell.innerHTML;

        const regexPattern = new RegExp(uniqueQueryList.join("|"), "gi");

        cellText = cellText.replace(
          regexPattern,
          (match) => `<mark>${match}</mark>`,
        );
        cell.innerHTML = cellText;
      }
    }
  }

  //  delete duplicate strings and also remove substrings from the list
  function removeDuplicatesAndSubstrings(query) {
    const subQueryList = query.split(/\s+/);

    // Sort the list by length in descending order
    const sortedList = subQueryList.sort((a, b) => b.length - a.length);

    const uniqueList = [];

    for (const str of sortedList) {
      if (
        !uniqueList.some((existingStr) =>
          existingStr.toLowerCase().includes(str.toLowerCase()),
        )
      ) {
        uniqueList.push(str);
      }
    }

    return uniqueList;
  }

  // New method to handle fetch and actions related to fileParents
  function fetchFileParents(fileUrl) {
    // Fetch the fileParents data
    fetch(`${fileUrl}/parents`, {
      method: "Get",
    })
      .then((response) => response.json())
      .then((data) => {
        // Get the reference to the <ul> element where we'll add the file paths
        const selectedFilePaths = document.getElementById(
          "selected-file-paths",
        );
        selectedFilePaths.innerHTML = "";

        // Add "Selected File: " in the front
        const li = document.createElement("li");
        li.textContent = "Selected File: ";
        selectedFilePaths.appendChild(li);

        // Loop through the fileParents data (List<Map<String, String>>)
        for (const entry of data) {
          // 'entry' is a map containing 'ancestorUUID' and 'filename' keys
          const li = document.createElement("li");
          const a = document.createElement("a");

          // Access the 'ancestorUUID' and 'filename' values from the map
          const ancestorUUID = entry["ancestorUUID"];
          const filename = entry["filename"];

          // Set the href and text content using Thymeleaf expressions
          a.setAttribute("href", `/ehz/files/${ancestorUUID}`);
          a.textContent = filename;

          li.appendChild(a);
          selectedFilePaths.appendChild(li);
        }
        // Hide the current file paths
        const currentFilePaths = document.getElementById("current-file-paths");
        currentFilePaths.style.display = "none";
        // Show the selected file paths
        selectedFilePaths.style.display = "block";
      })
      .catch((error) => {
        console.error("Error fetching fileParents:", error);
      });
  }

  function handleSelectedFileClick(event) {
    const targetRow = event.target.closest("tr");

    const fileUrl = targetRow.querySelector(".file-link").getAttribute("href");

    // Show the selected files' paths
    fetchFileParents(fileUrl);
  }

  function handleUnselectedFileClick(event) {
    const target = event.target;
    if (!tableContent.contains(target)) {
      const selectedFilePaths = document.getElementById("selected-file-paths");
      const currentFilePaths = document.getElementById("current-file-paths");

      //  Swap the display properties
      selectedFilePaths.style.display = "none";
      currentFilePaths.style.display = "block";
    }
  }

  function handleSelectedFileHover(event) {
    const targetRow = event.target;

    const hoverFileUrl = targetRow
      .querySelector(".file-link")
      .getAttribute("href");
    fetchFileParents(hoverFileUrl);
  }

  function handleUnselectedFileHover() {
    const tableRows = tableContent.querySelectorAll("tr");

    let isContainFocus = false;
    for (const row of tableRows) {
      if (row.classList.contains("focus")) {
        isContainFocus = true;
        const focusedFileUrl = row
          .querySelector(".file-link")
          .getAttribute("href");
        fetchFileParents(focusedFileUrl);
        break;
      }
    }
    if (!isContainFocus) {
      const selectedFilePaths = document.getElementById("selected-file-paths");
      const currentFilePaths = document.getElementById("current-file-paths");

      //  Swap the display properties
      selectedFilePaths.style.display = "none";
      currentFilePaths.style.display = "block";
    }
  }

  document.addEventListener("DOMContentLoaded", highlightSearch);
  document.addEventListener("DOMContentLoaded", () => {
    tableContent.addEventListener("click", handleSelectedFileClick);
    document.addEventListener("click", handleUnselectedFileClick);
    tableContent.addEventListener("mouseleave", handleUnselectedFileHover);

    // Get all the rows within the table
    const rows = tableContent.getElementsByTagName("tr");

    // Iterate through each row and add the Hover Event Listener
    for (let row of rows) {
      row.addEventListener("mouseenter", handleSelectedFileHover);
    }
  });
}
