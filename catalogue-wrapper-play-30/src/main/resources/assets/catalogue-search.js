// Catalogue search bar — reads quicksearch URL from data attribute, calls endpoint, renders results.
// TODO: Consider switching to backend-rendered HTML results for better accessibility.
(function () {
  "use strict";

  const bar = document.getElementById("catalogue-search-bar");
  const searchContainer = document.getElementById("catalogue-search-box");
  const input = document.getElementById("catalogue-search");
  const matches = document.getElementById("catalogue-search-matches");
  const mainmenu = document.getElementById("main-menu-bar");
  const layoutContainer = document.getElementById("standard-layout-container");

  if (!bar || !input || !matches) return;

  const quickSearchUrl = bar.dataset.quicksearchUrl || "/quicksearch";
  const minSearchLen = Number(bar.dataset.minSearchLen || "3");

  let selectedItem = 0;
  let resultCount  = 0;
  let debounceTimer;

  function escapeHtml(value) {
    return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function renderResults(results) {
    if (results.length === 0) {
      return (
        '<table style="width:100%">' +
          '<tbody class="catalogue-search-matches-body">' +
            "<tr><td>No results found</td></tr>" +
          "</tbody>" +
        "</table>"
      );
    }

    const rows = results
      .map(function (r, i) {
        const target = r.openInNewWindow
          ? ' target="_blank" rel="noreferrer noopener"'
          : "";
        return (
          "<tr>" +
            "<td>" +
              '<a id="search-item-' + i + '" href="' + escapeHtml(r.href) + '"' + target + ">" +
                escapeHtml(r.name) +
              "</a>" +
            "</td>" +
            '<td><strong class="search-item-type">' +
              escapeHtml(r.linkType.toLowerCase()) +
            "</strong></td>" +
          "</tr>"
        );
      })
      .join("");

    return (
      '<table style="width:100%">' +
        '<tbody class="catalogue-search-matches-body">' + rows + "</tbody>" +
      "</table>"
    );
  }

  function doSearch(q) {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(function () {
      const url = new URL(quickSearchUrl, window.location.origin);
      url.searchParams.set("query", q);

      const xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function () {
        if (xhr.readyState !== 4) return;
        if (xhr.status === 200) {
          const results = JSON.parse(xhr.responseText);
          matches.innerHTML = renderResults(results);
          resultCount  = results.length;
          selectedItem = 0;
          highlight(0);
        } else {
          matches.innerHTML =
            '<table style="width:100%"><tbody class="catalogue-search-matches-body">' +
            "<tr><td>Search unavailable</td></tr></tbody></table>";
          resultCount = 0;
        }
      };
      xhr.open("GET", url.toString());
      xhr.send();
    }, 250);
  }

  function toggleSearch() {
    if (searchContainer.classList.contains("search-width-initial")) {
      showSearchBar();
    } else {
      hideSearchBar();
    }
  }

  function showSearchBar() {
    bar.classList.remove("hidden-for-small-screens");
    searchContainer.classList.remove("search-width-initial");
    searchContainer.classList.add("search-width");
    if (mainmenu) mainmenu.classList.add("d-none");
    input.focus();
    input.value = "";
  }

  function hideSearchBar() {
    bar.classList.add("hidden-for-small-screens");
    searchContainer.classList.remove("search-width");
    searchContainer.classList.add("search-width-initial");
    matches.classList.add("d-none");
    if (mainmenu) mainmenu.classList.remove("d-none");
    input.value = "";
    selectedItem = -1;
    resultCount  = 0;
    matches.innerHTML = "";
  }

  function highlight(pos) {
    const item = document.getElementById("search-item-" + pos);
    if (item) item.parentElement.parentElement.classList.add("search-match-selected");
  }

  function unhighlight(pos) {
    const item = document.getElementById("search-item-" + pos);
    if (item) item.parentElement.parentElement.classList.remove("search-match-selected");
  }

  const searchIcon = document.getElementById("searchicon");
  if (searchIcon) {
    searchIcon.addEventListener("click", function (e) {
      e.stopImmediatePropagation();
      toggleSearch();
    }, true);
  }

  input.addEventListener("focus", function (e) {
    e.stopImmediatePropagation();
    showSearchBar();
  }, true);

  input.addEventListener("keyup", function (e) {
    if (e.keyCode === 13) {
      const firstItem = document.getElementById("search-item-" + selectedItem);
      if (firstItem) firstItem.click();
    } else if (e.keyCode === 38) {
      if (selectedItem > 0) {
        unhighlight(selectedItem);
        selectedItem--;
        highlight(selectedItem);
      }
    } else if (e.keyCode === 40) {
      if (selectedItem < resultCount - 1) {
        unhighlight(selectedItem);
        selectedItem++;
        highlight(selectedItem);
      }
    } else if (e.keyCode === 27) {
      hideSearchBar();
    } else if (e.keyCode > 40 || e.keyCode < 33) {
      if (e.target.value.length >= minSearchLen) {
        matches.classList.remove("d-none");
        doSearch(e.target.value);
      } else {
        matches.innerHTML = "";
        matches.classList.add("d-none");
      }
    }
  }, false);

  input.addEventListener("keydown", function (e) {
    if (e.key === "ArrowUp" || e.key === "ArrowDown") e.preventDefault();
  }, false);

  if (layoutContainer) {
    layoutContainer.addEventListener("click", function () { hideSearchBar(); }, true);
  }

  document.addEventListener("keyup", function (e) {
    if (e.ctrlKey && e.key === " ") toggleSearch();
  }, false);
})();
