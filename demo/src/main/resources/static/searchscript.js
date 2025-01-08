let currentPage = 1;

document.getElementById("searchInput").addEventListener("input", function (e) {
});

document.getElementById("searchForm").addEventListener("submit", function (e) {
    e.preventDefault();
    fetchMovies(1);
});

function fetchMovies(page) {
    const keyword = document.getElementById("searchInput").value.trim();

    if (!keyword) {
        displayErrorMessage("Please enter a movie name.");
        return;
    }

    clearErrorMessage();

    fetch("/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ keyword, page }),
    })
        .then(res => {
            if (res.redirected) {

                window.location.href = res.url;
                return;
            }

            return res.json();
        })
        .then(data => {
            if (data) {

                displayResults(data, page);
            }
        })
        .catch(error => {
            console.error("Error fetching results:", error);
            displayErrorMessage("An error occurred while fetching results. Please try again later.");
        });
}

function displayResults(results, page) {
    const resultsDiv = document.getElementById("results");
    resultsDiv.innerHTML = "";

    if (Object.keys(results).length === 0) {
        resultsDiv.innerHTML = "<p>No results found.</p>";
        return;
    }

    Object.entries(results).forEach(([title, url]) => {
        resultsDiv.innerHTML += `<div><a href="/ranking?movie=${encodeURIComponent(title)}">${title}</a></div>`;
    });

    currentPage = page;
    document.getElementById("prevPage").disabled = currentPage === 1;
    document.getElementById('prevPage').style.display = 'block';
    document.getElementById('nextPage').style.display = 'block';
}

function changeStyle() {
    const div = document.getElementById('containerspecial');
    div.classList.add('container');
    const outerDiv = document.getElementById("left");
    const innerDiv = document.getElementById("box");

    // 将 innerDiv 作为 outerDiv 的子元素
    outerDiv.appendChild(innerDiv);
    document.getElementById('left').style.width = '45%';
    document.getElementById('right').style.width = '55%';

}



function displayErrorMessage(message) {
    const errorMessageDiv = document.getElementById("errorMessage");
    errorMessageDiv.textContent = message;
}

function clearErrorMessage() {
    const errorMessageDiv = document.getElementById("errorMessage");
    errorMessageDiv.textContent = "";
}

document.getElementById("prevPage").onclick = () => fetchMovies(currentPage - 1);
document.getElementById("nextPage").onclick = () => fetchMovies(currentPage + 1);

