document.addEventListener("DOMContentLoaded", () => {
    const notifBtn = document.getElementById("notif-btn");
    const notifList = document.getElementById("notif-list");
    const notifItems = document.getElementById("notif-items");
    const notifDot = document.getElementById("notif-dot");

    // Toggle dropdown
    notifBtn.addEventListener("click", () => {
        notifList.classList.toggle("hidden");
        notifDot.classList.add("hidden"); // clear red dot when opened
    });

    // Load persisted notifications on page load
    fetch("/notifications/unread")
        .then(res => res.json())
        .then(data => {
            if (data.length > 0) {
                notifDot.classList.remove("hidden");
                data.forEach(n => addNotificationItem(n));
            }
        });

    // WebSocket setup
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        stompClient.subscribe("/user/queue/notifications", (message) => {
            const notif = JSON.parse(message.body);
            addNotificationItem(notif);
            notifDot.classList.remove("hidden");
        });
    });

    // Helper to add notification item
    function addNotificationItem(notif) {
        const li = document.createElement("li");
        li.className = "p-3 text-sm text-gray-700 hover:bg-gray-50";
        li.innerHTML = `<a href="/posts/${notif.postId}"><b>${notif.author}</b> published: <i>${notif.title}</i></a>`;
        notifItems.prepend(li);
    }
});