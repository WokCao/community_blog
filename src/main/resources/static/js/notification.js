document.addEventListener("DOMContentLoaded", () => {
    const notifBtn = document.getElementById("notif-btn");
    const notifList = document.getElementById("notif-list");
    const notifItems = document.getElementById("notif-items");
    const notifDot = document.getElementById("notif-dot");
    const viewAllBtn = document.getElementById("view-all-btn");
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    // WebSocket setup
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        stompClient.subscribe("/user/queue/notifications", (message) => {
            const notif = JSON.parse(message.body);
            addNotificationItem(notif);
            notifDot.classList.remove("hidden");
            notifList.classList.remove("hidden");
        });
    });

    if (notifBtn === null || notifList === null || notifItems === null || notifDot === null || viewAllBtn === null) {
        return;
    }
    // Toggle dropdown
    notifBtn.addEventListener("click", () => {
        notifList.classList.toggle("hidden");
        notifDot.classList.add("hidden"); // clear red dot when opened

        if (!notifList.classList.contains("hidden")) {
            // Load persisted notifications on page load
            fetch("/notifications?size=6&sortDir=desc")
                .then(res => res.json())
                .then(data => {
                    notifItems.innerHTML = "";

                    if (data.length > 0) {
                        notifDot.classList.remove("hidden");
                        notifList.classList.remove("hidden");

                        data.forEach(n => addNotificationItem(n));

                        if (data.length > 6) {
                            viewAllBtn.classList.remove("hidden");
                        } else {
                            viewAllBtn.classList.add("hidden");
                        }
                    } else {
                        notifDot.classList.add("hidden");
                        notifList.classList.remove("hidden");

                        const li = document.createElement("li");
                        li.className = "p-3 text-sm text-center text-gray-500 italic";
                        li.textContent = "There are no notifications.";
                        notifItems.appendChild(li);

                        viewAllBtn.classList.add("hidden");
                    }
                });
        }
    });

    // Helper to add notification item
    function addNotificationItem(notif) {
        const li = document.createElement("li");
        li.className = `flex items-center gap-3 p-3 text-sm text-gray-700 hover:bg-gray-50 transition-all border-b border-gray-100 ${!notif.read ? 'bg-blue-50' : ''}`;

        const a = document.createElement("a");
        a.href = `/posts/${notif.postId}`;
        a.className = "flex items-center w-full";

        a.innerHTML = `
          <div class="flex-shrink-0 w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center mr-3">
              <img src="${notif.avatarUrl}" alt="Avatar" loading="lazy" class="w-6 h-6 rounded-full object-cover">
          </div>
          <div class="flex-1 line-clamp-3">
              <span class="font-semibold text-indigo-700">${notif.author}</span>
              <span class="mx-1 text-gray-400">•</span>
              <span class="text-gray-800">published:</span>
              <span class="font-bold">${notif.title}</span>
          </div>
          ${notif.time ? `<span class='ml-2 text-xs text-gray-400'>${notif.time}</span>` : ""}
        `;

        a.addEventListener("click", async (e) => {
            e.preventDefault(); // stop instant navigation

            try {
                await fetch(`/notifications/${notif.notificationId}/markAsRead`, {
                    method: "PATCH",
                    headers: {
                        "Content-Type": "application/json",
                        [header]: token
                    }
                });
            } catch (err) {
                console.error("Failed to mark notification as read", err);
            }

            window.location.href = a.href;
        });

        li.appendChild(a);
        notifItems.appendChild(li);
    }
});