document.getElementById("searchForm").addEventListener("submit", function (event) {
    event.preventDefault();
    const searchTerm = document.getElementById("searchInput").value; 
    const selectedLanguage = document.getElementById("languageSelect").value;

	function fetchResults(searchTerm, selectedLanguage) {
	    fetch("/search", {
	        method: "POST",
	        headers: { "Content-Type": "application/json" },
	        body: JSON.stringify({ keyword: searchTerm, language: selectedLanguage, page: 1 })
	    })
	    .then(response => {
	        if (!response.ok) {
	            throw new Error("Error fetching search results");
	        }
	        return response.json();
	    })
	    .then(data => {
	        console.log("Fetched data:", data);
	        displayResults(data.results);
	    })
	    .catch(error => {
	        console.error("Error:", error);
	    });
	}

	function displayResults(results) {
	    const resultsDiv = document.getElementById("results");
	    resultsDiv.innerHTML = "";

	    if (results && results.length === 0) {
	        resultsDiv.innerHTML = "<p>No movie results found.</p>";
	    } else {
	        results.forEach((movie) => {
	            const resultItem = document.createElement("div");
	            resultItem.classList.add("result-item");

	            const resultTitle = document.createElement("a");
	            resultTitle.classList.add("result-title");
	            resultTitle.textContent = movie.title;
	            resultTitle.href = movie.googleSearchUrl;
	            resultTitle.target = "_blank";

	            resultItem.appendChild(resultTitle);
	            resultsDiv.appendChild(resultItem);
	        });
	    }
	}










