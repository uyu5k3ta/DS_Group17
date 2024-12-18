let currentPage = 1; // 當前頁面

// 發送請求以獲取電影列表
function fetchMovies(page) {
    const searchTerm = document.getElementById("searchInput").value; // 讀取輸入的關鍵字

    fetch("/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ keyword: searchTerm, page: page }), // 傳遞關鍵字和頁碼
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("Error fetching movies");
            }
            return response.json();
        })
        .then((data) => {
            displayResults(data); // 顯示結果
            togglePaginationButtons(page); // 切換按鈕狀態
        })
        .catch((error) => {
            console.error("Error fetching results:", error);
        });
}

// 顯示電影列表
function displayResults(results) {
    const resultsDiv = document.getElementById("results");
    resultsDiv.innerHTML = ""; // 清空先前的結果

    if (Object.keys(results).length === 0) {
        resultsDiv.innerHTML = "<p>No movie results found.</p>"; // 如果沒有結果顯示提示
    } else {
        Object.entries(results).forEach(([title, url]) => {
            const resultItem = document.createElement("div");
            resultItem.classList.add("result-item");

            const resultLink = document.createElement("a");
            resultLink.href = "/ranking.html?movie=" + encodeURIComponent(title); // 跳轉到 ranking.html 並傳遞電影名稱
            resultLink.textContent = title;

            resultItem.appendChild(resultLink);
            resultsDiv.appendChild(resultItem); // 添加到結果區域
        });
    }
}

// 切換翻頁按鈕的狀態
function togglePaginationButtons(page) {
    document.getElementById("prevPage").disabled = page === 1; // 當前是第一頁時禁用上一頁按鈕
}

// 搜索表單的提交事件
document.getElementById("searchForm").addEventListener("submit", function (event) {
    event.preventDefault(); // 阻止默認提交行為
    currentPage = 1; // 重置為第一頁
    fetchMovies(currentPage); // 獲取電影
});

// 上一頁按鈕的點擊事件
document.getElementById("prevPage").addEventListener("click", function () {
    if (currentPage > 1) {
        currentPage--;
        fetchMovies(currentPage); // 獲取上一頁的電影
    }
});

// 下一頁按鈕的點擊事件
document.getElementById("nextPage").addEventListener("click", function () {
    currentPage++;
    fetchMovies(currentPage); // 獲取下一頁的電影
});
