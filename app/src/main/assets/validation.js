function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePassword(password) {
    return password.length >= 6;
}

function validateLogin(email, password) {
    if (!validateEmail(email)) {
        return "Invalid email format";
    }
    if (!validatePassword(password)) {
        return "Password must be at least 6 characters";
    }
    return "OK";
}
