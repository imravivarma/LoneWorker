function validateEmail(email) {
    const re = /^[^\s@]+@gdi\.com$/;
    return re.test(email);
}

function validatePassword(password) {
    return password.length >= 8;
}

function validateLogin(email, password) {
    if (!validateEmail(email)) {
        return "Email must end with @gdi.com";
    }
    if (!validatePassword(password)) {
        return "Password must be at least 8 characters";
    }
    return "OK";
}
