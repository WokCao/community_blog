const avatarList = [
    "bear.png",
    "cat.png",
    "chicken.png",
    "deer.png",
    "dog.png",
    "dragon.png",
    "giraffe.png",
    "koala.png",
    "lion.png",
    "meerkat.png",
    "panda.png",
    "puffer-fish.png",
    "rabbit.png",
    "sea-lion.png",
    "sloth.png",
    "weasel.png",
    "wolf.png"
]

function openAvatarPicker() {
    const grid = document.getElementById("avatarGrid");
    grid.innerHTML = "";

    avatarList.forEach(img => {
        const imgElement = document.createElement("img");
        imgElement.src = `/img/${img}`;
        imgElement.className = "w-20 h-20 rounded-full cursor-pointer border-0 hover:border-indigo-600";
        imgElement.onclick = () => selectAvatar(img);
        imgElement.title = img.split('.')[0].charAt(0).toUpperCase() + img.split('.')[0].slice(1);
        grid.appendChild(imgElement);
    });

    document.body.classList.add('overflow-hidden');
    document.getElementById('avatarModal').classList.remove('hidden');
    document.getElementById('avatarModal').classList.add('flex');
    document.getElementsByClassName('backdrop')[0].classList.remove('hidden');
    document.getElementsByClassName('backdrop')[0].classList.add('bg-black', 'opacity-40');
}

function closeAvatarPicker() {
    document.body.classList.remove('overflow-hidden');
    document.getElementById('avatarModal').classList.add('hidden');
    document.getElementById('avatarModal').classList.remove('flex');
    document.getElementsByClassName('backdrop')[0].classList.remove('bg-black', 'opacity-40');
    document.getElementsByClassName('backdrop')[0].classList.add('hidden');
}

function selectAvatar(path) {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    fetch('/users/me/update-avatar', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({avatarUrl: path}),
        credentials: 'include'
    })
        .then(data => {
            if (data.ok) {
                location.reload();
            } else {
                alert('Failed to update avatar');
            }
        })
        .catch(err => {
            console.error(err);
            alert('Error updating avatar');
        });
}
