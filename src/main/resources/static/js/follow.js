document.addEventListener("DOMContentLoaded", () => {
    const followBtn = document.getElementById("follow-btn");
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    if (followBtn) {
        followBtn.addEventListener("click", async () => {
            const userId = followBtn.dataset.userId;
            const action = followBtn.dataset.action;

            let url = `/follows/${userId}`;
            let options = {
                method: action === "follow" ? "POST" : "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    [header]: token
                }
            };

            try {
                const res = await fetch(url, options);
                if (res.ok) {
                    // Toggle state
                    if (action === "follow") {
                        followBtn.dataset.action = "unfollow";
                        followBtn.querySelector("span").textContent = "Unfollow";
                        followBtn.classList.remove("bg-blue-500", "hover:bg-blue-600");
                        followBtn.classList.add("bg-gray-500", "hover:bg-gray-600");
                    } else {
                        followBtn.dataset.action = "follow";
                        followBtn.querySelector("span").textContent = "Follow";
                        followBtn.classList.remove("bg-gray-500", "hover:bg-gray-600");
                        followBtn.classList.add("bg-blue-500", "hover:bg-blue-600");
                    }
                } else {
                    window.location.href = "/auth/login";
                }
            } catch (err) {
                console.error("Fetch error:", err);
            }
        });
    }
});