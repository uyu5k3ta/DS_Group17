function loadStyle(url) {
	var link = document.createElement('link')
	link.type = 'text/css'
	link.rel = 'stylesheet'
	link.href = url
	var head = document.getElementsByTagName('head')[0]
	head.appendChild(link)
}
loadStyle('styles.css')

document.getElementById("searchForm").addEventListener("submit", function (event) {
	event.preventDefault();
	const searchTerm = document.getElementById("searchInput").value;
	const selectedLanguage = document.getElementById("languageSelect").value;

	fetchResults(searchTerm, selectedLanguage);
});

function fetchResults(searchTerm, selectedLanguage) {
	document.getElementById("loading").style.display = "block";

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
			document.getElementById("loading").style.display = "none";
			displayResults(data.results);
		})
		.catch(error => {
			document.getElementById("loading").style.display = "none";
			console.error("Error:", error);
		});
}

function displayResults(results) {
	const resultsDiv = document.getElementById("results");
	resultsDiv.innerHTML = "";

	if (results.length === 0) {
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