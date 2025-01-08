let currentPage = 1;
const movieName = "[[${movie}]]";

fetchResults(currentPage);

document.getElementById("prevPage").addEventListener("click", () => {
    if (currentPage > 1) {
        currentPage--;
        fetchResults(currentPage);
    }
});

document.getElementById("nextPage").addEventListener("click", () => {
    currentPage++;
    fetchResults(currentPage);
});

function fetchResults(page) {

    togglePaginationButtons(false);
    document.getElementById("loading").style.display = "block";
    document.getElementById("results").innerHTML = "";

    fetch("/ranked-urls", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ keyword: movieName, page })
    })
        .then(res => res.json())
        .then(data => {
            const results = document.getElementById("results");
            const relatedKeywordsDiv = document.getElementById("relatedKeywords");


            results.innerHTML = "";
            relatedKeywordsDiv.innerHTML = "<strong>relative word:</strong><br>";


            if (data.siteData) {
                Object.keys(data.siteData).forEach(url => {
                    const motherPage = document.createElement("div");
                    motherPage.innerHTML = `<a href="${url}" target="_blank">${url}</a>`;
                    results.appendChild(motherPage);
                });
            } else {
                results.textContent = "No mother pages found.";
            }

            if (data.relatedKeywords && data.relatedKeywords.length > 0) {
                data.relatedKeywords.forEach(keyword => {
                    const key = keyword.key || "Unknown";
                    const keywordSpan = document.createElement("span");
                    keywordSpan.textContent = key;
                    relatedKeywordsDiv.appendChild(keywordSpan);
                });
            } else {
                relatedKeywordsDiv.innerHTML += "No related keywords found.";
            }

            document.getElementById("loading").style.display = "none";
            togglePaginationButtons(true);

            document.getElementById("prevPage").disabled = page === 1;
        })
        .catch(error => {
            console.error("Error fetching ranking results:", error);


            document.getElementById("loading").style.display = "none";
            togglePaginationButtons(true);
        });
}

function togglePaginationButtons(enabled) {
    document.getElementById("prevPage").disabled = !enabled;
    document.getElementById("nextPage").disabled = !enabled;
}
