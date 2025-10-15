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
                        followBtn.querySelector("span").textContent = "Following";

                        // Change icon
                        const icon = followBtn.querySelector("i");
                        icon.classList.remove("fa-plus");
                        icon.classList.add("fa-check");

                        // Update styling: gradient → gray
                        followBtn.classList.remove(
                            "bg-gradient-to-r",
                            "from-indigo-600",
                            "to-purple-600",
                            "text-white",
                            "hover:from-indigo-700",
                            "hover:to-purple-700"
                        );
                        followBtn.classList.add(
                            "bg-gray-200",
                            "text-gray-700",
                            "hover:bg-gray-300"
                        );
                    } else {
                        followBtn.dataset.action = "follow";
                        followBtn.querySelector("span").textContent = "Follow";

                        // Change icon
                        const icon = followBtn.querySelector("i");
                        icon.classList.remove("fa-check");
                        icon.classList.add("fa-plus");

                        // Update styling: gray → gradient
                        followBtn.classList.remove(
                            "bg-gray-200",
                            "text-gray-700",
                            "hover:bg-gray-300"
                        );
                        followBtn.classList.add(
                            "bg-gradient-to-r",
                            "from-indigo-600",
                            "to-purple-600",
                            "text-white",
                            "hover:from-indigo-700",
                            "hover:to-purple-700"
                        );
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