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

    // Handle comment form
    document.querySelectorAll(".add-comment-form").forEach(form => {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const postId = form.dataset.postId;
            const commentContent = form.querySelector("textarea[name='content']").value;

            if (!commentContent.trim()) {
                alert("Comment cannot be empty.");
                return;
            }

            console.log(commentContent)

            const res = await fetch(`/posts/${postId}/comment`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    [header]: token
                },
                body: JSON.stringify({'content': commentContent})
            });
            if (res.ok) {
                const data = await res.json();
                form.querySelector("textarea[name='content']").value = "";
                const commentView = data.newCommentView;
                console.log(commentView)

                const template = `
                <div class="m-0 p-0 w-full flex space-x-4">
                    <img src="${commentView.comment.commenter.avatarUrl}" alt="${commentView.comment.commenter.fullName}"
                         class="w-10 h-10 rounded-full object-cover flex-shrink-0">
                    <div class="flex-1">
                        <div class="rounded-lg bg-gray-50 p-4">
                            <div class="flex items-center space-x-2 mb-2">
                                <h4 class="font-semibold text-gray-900">${commentView.comment.commenter.fullName}</h4>
                                <span class="text-sm text-gray-500">${commentView.comment.timeAgo}</span>
                            </div>
                            <p class="text-gray-700">${commentView.comment.content}</p>
            
                            <div class="flex items-center justify-between pt-4">
                                <div class="flex items-center space-x-1">
                                    <!-- Like Button -->
                                    <form action="/comments/${commentView.comment.id}/like" method="post"
                                          class="inline">
                                        <button type="submit"
                                                disabled="disabled"
                                                class="group flex items-center space-x-1 px-3 py-2 rounded-md text-sm font-medium transition-all duration-200 hover:cursor-pointer ${commentView.liked ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100' : 'text-gray-500 hover:bg-emerald-100 hover:text-emerald-600'}">
                                            <svg class="w-4 h-4 transition-transform group-hover:scale-110"
                                                 fill="currentColor" viewBox="0 0 24 24">
                                                <path d="M7.493 18.75c-.425 0-.82-.236-.975-.632A7.48 7.48 0 016 15.375c0-1.75.599-3.358 1.602-4.634.151-.192.373-.309.6-.397.473-.183.89-.514 1.212-.924a9.042 9.042 0 012.861-2.4c.723-.384 1.35-.956 1.653-1.715a4.498 4.498 0 00.322-1.672V3a.75.75 0 01.75-.75 2.25 2.25 0 012.25 2.25c0 1.152-.26 2.243-.723 3.218-.266.558-.107 1.282.725 1.282h3.126c1.026 0 1.945.694 2.054 1.715.045.422.068.85.068 1.285a11.95 11.95 0 01-2.649 7.521c-.388.482-.987.729-1.605.729H14.23c-.483 0-.964-.078-1.423-.23l-3.114-1.04a4.501 4.501 0 00-1.423-.23h-.777zM2.331 10.977a11.969 11.969 0 00-.831 4.398 12 12 0 00.52 3.507c.26.85 1.084 1.368 1.973 1.368H4.9c.445 0 .72-.498.523-.898a8.963 8.963 0 01-.924-3.977c0-1.708.476-3.305 1.302-4.666.245-.403-.028-.959-.5-.959H4.25c-.832 0-1.612.453-1.918 1.227z"/>
                                            </svg>
                                            <span>${commentView.comment.likeCount || 0}</span>
                                        </button>
                                    </form>
            
                                    <!-- Dislike Button -->
                                    <form action="/comments/${commentView.comment.id}/dislike" method="post"
                                          class="inline">
                                        <button type="submit"
                                                disabled="disabled"
                                                class="group flex items-center space-x-1 px-3 py-2 rounded-md text-sm font-medium transition-all duration-200 hover:cursor-pointer ${commentView.disliked ? 'bg-red-50 text-red-700 hover:bg-red-100' : 'text-gray-500 hover:bg-red-50 hover:text-red-600'}">
                                            <svg class="w-4 h-4 rotate-180 transition-transform group-hover:scale-110"
                                                 fill="currentColor" viewBox="0 0 24 24">
                                                <path d="M7.493 18.75c-.425 0-.82-.236-.975-.632A7.48 7.48 0 016 15.375c0-1.75.599-3.358 1.602-4.634.151-.192.373-.309.6-.397.473-.183.89-.514 1.212-.924a9.042 9.042 0 012.861-2.4c.723-.384 1.35-.956 1.653-1.715a4.498 4.498 0 00.322-1.672V3a.75.75 0 01.75-.75 2.25 2.25 0 012.25 2.25c0 1.152-.26 2.243-.723 3.218-.266.558-.107 1.282.725 1.282h3.126c1.026 0 1.945.694 2.054 1.715.045.422.068.85.068 1.285a11.95 11.95 0 01-2.649 7.521c-.388.482-.987.729-1.605.729H14.23c-.483 0-.964-.078-1.423-.23l-3.114-1.04a4.501 4.501 0 00-1.423-.23h-.777zM2.331 10.977a11.969 11.969 0 00-.831 4.398 12 12 0 00.52 3.507c.26.85 1.084 1.368 1.973 1.368H4.9c.445 0 .72-.498.523-.898a8.963 8.963 0 01-.924-3.977c0-1.708.476-3.305 1.302-4.666.245-.403-.028-.959-.5-.959H4.25c-.832 0-1.612.453-1.918 1.227z"/>
                                            </svg>
                                            <span>${commentView.comment.dislikeCount || 0}</span>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                `
                const commentList = document.getElementsByClassName('comment-list')[0];
                commentList.insertAdjacentHTML('beforebegin', template);

            }
        });
    });

});