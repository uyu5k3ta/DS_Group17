document.getElementById("searchForm").addEventListener("submit", function (event) {
    event.preventDefault();
    const searchTerm = document.getElementById("searchInput").value; 

    fetch("/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ keyword: searchTerm }),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("網路回應錯誤");
            }
            return response.json(); 
        })
        .then((data) => {
            displayResults(data);
        })
        .catch((error) => {
            console.error("Error fetching results:", error);
        });


    function displayResults(results) {
        const resultsDiv = document.getElementById("results");
        resultsDiv.innerHTML = "";

        if (Object.keys(results).length === 0) {
            resultsDiv.innerHTML = "<p>没有找到相關結果。</p>";
        } else {

            for (const [title, url] of Object.entries(results)) {
                const resultItem = document.createElement("div");
                resultItem.classList.add("result-item");


                const resultTitle = document.createElement("button");
                resultTitle.classList.add("result-title");
                resultTitle.textContent = title;


                resultTitle.addEventListener("click", () => {
                    window.open(url, "_blank");
                });

                resultItem.appendChild(resultTitle);
                resultsDiv.appendChild(resultItem);
            }
        }
    }
});




