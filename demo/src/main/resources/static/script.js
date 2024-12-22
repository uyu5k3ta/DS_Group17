let currentPage = 1;

document.getElementById("searchForm").addEventListener("submit", function (event) {
    event.preventDefault();
    currentPage = 1;
    fetchMovies(currentPage);
});

function fetchMovies(page) {
    const searchTerm = document.getElementById("searchInput").value;

    fetch("/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ keyword: searchTerm, page: page }),
    })
        .then((response) => response.json())
        .then((data) => displayResults(data))
        .catch((error) => console.error("Error fetching results:", error));
}

function displayResults(results) {
    const resultsDiv = document.getElementById("results");
    resultsDiv.innerHTML = "";

    Object.entries(results).forEach(([title, url]) => {
        const resultItem = document.createElement("div");
        resultItem.classList.add("result-item");

        const resultLink = document.createElement("a");
        resultLink.href = "/ranking?movie=" + encodeURIComponent(title);
        resultLink.textContent = title;

        resultItem.appendChild(resultLink);
        resultsDiv.appendChild(resultItem);
    });
}

document.getElementById("prevPage").addEventListener("click", function () {
    if (currentPage > 1) {
        currentPage--;
        fetchMovies(currentPage);
    }
});

document.getElementById("nextPage").addEventListener("click", function () {
    currentPage++;
    fetchMovies(currentPage);
});



document.getElementById("prevPage").addEventListener("click", function () {
    if (currentPage > 1) {
        currentPage--;
        fetchMovies(currentPage);
    }
});

document.getElementById("nextPage").addEventListener("click", function () {
    currentPage++;
    fetchMovies(currentPage);
});
