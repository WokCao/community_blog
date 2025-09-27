document.getElementById("updateNameForm").addEventListener("submit", async function (e) {
    e.preventDefault(); // stop normal form submit

    const fullName = document.getElementById("fullName").value;

    // Get CSRF token & header from meta tags
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    fetch("/users/me/update-fullname", {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            [header]: token
        },
        credentials: "include",
        body: JSON.stringify({fullName})
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert("Failed to update name");
            }
        })
        .catch(err => {
            console.error(err);
            alert('Error updating name');
        });
});