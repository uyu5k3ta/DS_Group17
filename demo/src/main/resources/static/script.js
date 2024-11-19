document.getElementById("searchForm").addEventListener("submit", function (event) {
    event.preventDefault(); // 阻止表單提交
    const searchTerm = document.getElementById("searchInput").value; // 獲取用戶輸入的關鍵字

    fetch("/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ keyword: searchTerm }), // 傳送關鍵字
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("網路回應錯誤");
            }
            return response.json(); // 返回 JSON 格式的回應
        })
        .then((data) => {
            displayResults(data); // 顯示搜尋結果
        })
        .catch((error) => {
            console.error("Error fetching results:", error);
        });

    // 顯示搜尋結果
    function displayResults(results) {
        const resultsDiv = document.getElementById("results");
        resultsDiv.innerHTML = ""; // 清空先前的結果

        if (Object.keys(results).length === 0) {
            resultsDiv.innerHTML = "<p>没有找到相關結果。</p>"; // 如果沒有結果，顯示提示訊息
        } else {
            // 遍歷結果並顯示標題
            for (const [title, url] of Object.entries(results)) {
                const resultItem = document.createElement("div");
                resultItem.classList.add("result-item");

                // 創建顯示標題的按鈕
                const resultTitle = document.createElement("button");
                resultTitle.classList.add("result-title");
                resultTitle.textContent = title;

                // 點擊標題時打開對應的電影網址
                resultTitle.addEventListener("click", () => {
                    window.open(url, "_blank");
                });

                resultItem.appendChild(resultTitle); // 將標題添加到結果項目
                resultsDiv.appendChild(resultItem); // 添加到結果區域
            }
        }
    }
});




