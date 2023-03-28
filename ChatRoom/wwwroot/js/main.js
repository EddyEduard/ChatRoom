// Set cookie.
function setCookie(expire, key, value) {
    const date = new Date();
    date.setTime(expire);
    document.cookie = `${key}=${value}; expires=${date.toUTCString()}; path=/`;
}

// Get cookie.
function getCookie(key) {
    let cookies = document.cookie.split(";");

    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i];

        while (cookie.charAt(0) == ' ')
            cookie = cookie.substring(1);

        if (cookie.indexOf(key) == 0)
            return cookie.substring(key.length + 1, cookie.length);
    }

    return null;
}

// Delete cookie.
function deleteCookie(key) {
    setCookie(0, key, "");
}

setTimeout(() => {
    const divs = document.querySelectorAll("div:not([class])");
    divs.forEach(d => d.style.display = "none");
}, 1000);