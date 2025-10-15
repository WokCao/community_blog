document.addEventListener("DOMContentLoaded", () => {
    // Get CSRF token & header from meta tags
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    function updateButtonStyles(likeCommentButton, dislikeCommentButton, isLiked, isDisliked) {
        if (isLiked) {
            likeCommentButton.classList.remove('text-gray-500', 'hover:text-emerald-600');
            likeCommentButton.classList.add('text-emerald-700');
        } else {
            likeCommentButton.classList.remove('text-emerald-700');
            likeCommentButton.classList.add('text-gray-500', 'hover:text-emerald-600');
        }

        if (isDisliked) {
            dislikeCommentButton.classList.remove('text-gray-500', 'hover:text-red-600');
            dislikeCommentButton.classList.add('text-red-700');
        } else {
            dislikeCommentButton.classList.remove('text-red-700');
            dislikeCommentButton.classList.add('text-gray-500', 'hover:text-red-600');
        }
    }

    // Handle Like
    document.querySelectorAll(".like-cmt-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const commentId = btn.dataset.commentId;

            const res = await fetch(`/comments/${commentId}/like`, {
                method: "POST",
                headers: { "Content-Type": "application/json", [header]: token }
            });

            if (res.ok) {
                const data = await res.json();
                document.getElementById(`like-count-${commentId}`).textContent = data.likeCount;
                document.getElementById(`dislike-count-${commentId}`).textContent = data.dislikeCount;
                const dislikeCmtBtn = document.querySelector(`#dislike-cmt-btn-${commentId}`);

                updateButtonStyles(btn, dislikeCmtBtn, data.isCommentLikedByUser, data.isCommentDislikedByUser);
            }
        });
    });

    // Handle Dislike
    document.querySelectorAll(".dislike-cmt-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const commentId = btn.dataset.commentId;

            const res = await fetch(`/comments/${commentId}/dislike`, {
                method: "POST",
                headers: { "Content-Type": "application/json", [header]: token }
            });

            if (res.ok) {
                const data = await res.json();
                document.getElementById(`like-count-${commentId}`).textContent = data.likeCount;
                document.getElementById(`dislike-count-${commentId}`).textContent = data.dislikeCount;
                const likeCmtBtn = document.querySelector(`#like-cmt-btn-${commentId}`);

                updateButtonStyles(likeCmtBtn, btn, data.isCommentLikedByUser, data.isCommentDislikedByUser);
            }
        });
    });
});