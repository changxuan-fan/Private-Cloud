// Define the function
async function updatePermissionButtons(userId) {
  const permissionButtons = document.querySelectorAll(".permission-button");

  try {
    // Fetch userFileMappings
    const response = await fetch("/ehz/admin/get-permissions");
    if (!response.ok) {
      throw new Error("Network response was not ok");
    }
    const userFileMappings = await response.json();

    permissionButtons.forEach((button) => {
      const fileId = button.parentNode.getAttribute("value");
      const permission = button.value;

      // Check if there's a mapping in userFileMappings with the same userId, fileId, and permission
      const mappingExists = userFileMappings.some((mapping) => {
        return (
          mapping.userId === userId &&
          mapping.fileId === fileId &&
          mapping.permission === permission
        );
      });

      if (mappingExists) {
        button.classList.add("clicked");
      } else {
        button.classList.remove("clicked");
      }
    });
  } catch (error) {
    console.error("Error fetching data:", error);
    // Handle the error, e.g., show an error message to the user
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const rows = document.querySelectorAll(
    "#permission-user-table .table-content tr",
  );

  rows.forEach((row) => {
    row.addEventListener("click", async function () {
      rows.forEach((r) => r.classList.remove("focus"));
      this.classList.add("focus");

      const userId = this.getAttribute("value");

      // Update the permission buttons when click on a user
      await updatePermissionButtons(userId);
    });
  });
});

// Execute the function when the DOM is loaded
document.addEventListener("DOMContentLoaded", async () => {
  // Get the first row of the table's tbody
  const table = document.getElementById("permission-user-table");
  const firstUserRow = table.querySelector("tbody tr:first-child");
  const firstUserId = firstUserRow.getAttribute("value");

  // Add focus to the User row
  firstUserRow.classList.add("focus");
  await updatePermissionButtons(firstUserId);
});

function sortTable(n) {
  let table,
    rows,
    switching,
    i,
    x,
    y,
    shouldSwitch,
    dir,
    switchCount = 0;
  table = document.getElementById("permission-user-table");
  switching = true;
  //Set the sorting direction to ascending:
  dir = "asc";
  /*Make a loop that will continue until
                                      no switching has been done:*/
  while (switching) {
    //start by saying: no switching is done:
    switching = false;
    rows = table.rows;
    /*Loop through all table rows (except the
                                                                            first, which contains table headers):*/
    for (i = 1; i < rows.length - 1; i++) {
      //start by saying there should be no switching:
      shouldSwitch = false;
      /*Get the two elements you want to compare,
                                                                                                                  one from current row and one from the next:*/
      x = rows[i].getElementsByTagName("TD")[n];
      y = rows[i + 1].getElementsByTagName("TD")[n];
      /*check if the two rows should switch place,
                                                                                                                  based on the direction, asc or desc:*/
      if (dir === "asc") {
        if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
          //if so, mark as a switch and break the loop:
          shouldSwitch = true;
          break;
        }
      } else if (dir === "desc") {
        if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
          //if so, mark as a switch and break the loop:
          shouldSwitch = true;
          break;
        }
      }
    }
    if (shouldSwitch) {
      /*If a switch has been marked, make the switch
                                                                                                                  and mark that a switch has been done:*/
      rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
      switching = true;
      //Each time a switch is done, increase this count by 1:
      switchCount++;
    } else {
      /*If no switching has been done AND the direction is "asc",
                                                                                                                  set the direction to "desc" and run the while loop again.*/
      if (switchCount === 0 && dir === "asc") {
        dir = "desc";
        switching = true;
      }
    }
  }
}
