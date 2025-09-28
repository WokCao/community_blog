document.addEventListener("DOMContentLoaded", () => {
    // Get CSRF token & header from meta tags
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    const likePostButton = document.querySelector(".like-post-form > button")
    const dislikePostButton = document.querySelector(".dislike-post-form > button")

    function updateButtonStyles(isLiked, isDisliked) {
        if (isLiked) {
            likePostButton.classList.remove('bg-gray-100', 'text-gray-600', 'hover:bg-green-100', 'hover:text-green-700');
            likePostButton.classList.add('bg-green-100', 'text-green-700');
        } else {
            likePostButton.classList.remove('bg-green-100', 'text-green-700');
            likePostButton.classList.add('bg-gray-100', 'text-gray-600', 'hover:bg-green-100', 'hover:text-green-700');
        }

        if (isDisliked) {
            dislikePostButton.classList.remove('bg-gray-100', 'text-gray-600', 'hover:bg-red-100', 'hover:text-red-700');
            dislikePostButton.classList.add('bg-red-100', 'text-red-700');
        } else {
            dislikePostButton.classList.remove('bg-red-100', 'text-red-700');
            dislikePostButton.classList.add('bg-gray-100', 'text-gray-600', 'hover:bg-red-100', 'hover:text-red-700');
        }
    }

    // Handle like form
    document.querySelectorAll(".like-post-form").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const postId = form.dataset.postId;

            const res = await fetch(`/posts/${postId}/like`, { method: "POST", headers: { [header]: token } });
            if (res.ok) {
                const data = await res.json();
                document.getElementById(`like-count-${postId}`).textContent = data.likeCount;
                document.getElementById(`dislike-count-${postId}`).textContent = data.dislikeCount;

                updateButtonStyles(data.isPostLikedByUser, data.isPostDislikedByUser);
            }
        });
    });

    // Handle dislike form
    document.querySelectorAll(".dislike-post-form").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const postId = form.dataset.postId;

            const res = await fetch(`/posts/${postId}/dislike`, { method: "POST", headers: { [header]: token } });
            if (res.ok) {
                const data = await res.json();
                document.getElementById(`like-count-${postId}`).textContent = data.likeCount;
                document.getElementById(`dislike-count-${postId}`).textContent = data.dislikeCount;

                updateButtonStyles(data.isPostLikedByUser, data.isPostDislikedByUser);
            }
        });
    });
});