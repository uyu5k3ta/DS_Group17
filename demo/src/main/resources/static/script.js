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
        .then((data) => {
            displayResults(data.results);
            displayRelatedKeywords(data.relatedKeywords);
        })
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

    document.getElementById("prevPage").disabled = currentPage === 1;
}

function displayRelatedKeywords(keywords) {
    const keywordsDiv = document.getElementById("relatedKeywords");
    keywordsDiv.innerHTML = "";
    keywords.forEach(keyword => {
        const keywordSpan = document.createElement("span");
        keywordSpan.textContent = keyword;
        keywordSpan.style.marginRight = "10px";
        keywordsDiv.appendChild(keywordSpan);
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

