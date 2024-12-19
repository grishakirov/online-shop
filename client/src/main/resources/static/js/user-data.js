
    document.addEventListener('DOMContentLoaded', () => {
    console.log("Fetching authenticated user...");
    fetch('http://localhost:8081/users/authenticated', {
    method: 'GET',
    headers: {
    'Content-Type': 'application/json'
},
    credentials: 'include'
})
    .then(response => {
    if (!response.ok) {
    return null;
}
    return response.json();
})
    .then(data => {
    console.log("User data:", data);
    const roleSpecificLinks = document.getElementById('role-specific-links');
    const authLinks = document.getElementById('auth-links');
    const contentDiv = document.getElementById('content');

    if (data && data.email) {
    // Update role-specific links
    if (data.role === 'ADMINISTRATOR') {
    roleSpecificLinks.innerHTML = `
                            <a href="/users">Manage Users</a>
                            <a href="/bonus-cards">Manage Bonus Cards</a>
                            <a href="/products-admin">Manage Products</a>
                            <a href="/orders-admin">Manage Orders</a>
                        `;
} else if (data.role === 'CUSTOMER') {
    roleSpecificLinks.innerHTML = `
                            <a href="/products-customer">Products</a>
                            <a href="/cart">Shopping Cart</a>
                            <a href="/personal-details">My Details</a>
                        `;
}

    // Update authentication links
    authLinks.innerHTML = `
                        <form action="http://localhost:8081/logout" method="POST" style="margin: 0;">
                            <button type="submit" class="btn" style="background:none; color:white; border:none; cursor:pointer;">Logout</button>
                        </form>
                    `;

    // Update content div
    contentDiv.innerHTML = `
                        <h2>Welcome, ${data.name || data.email}!</h2>
                        <div class="user-details">
                            <h3>Your Details:</h3>
                            <p><strong>Name:</strong> ${data.name}</p>
                            <p><strong>Email:</strong> ${data.email}</p>
                        </div>
                    `;
} else {
    // Update role-specific links for anonymous users
    roleSpecificLinks.innerHTML = '';
    authLinks.innerHTML = `
                        <a href="/login">Login</a>
                        <a href="/register">Register</a>
                    `;

    // Update content div for anonymous users
    contentDiv.innerHTML = `
                        <h2>Welcome to the Online Store</h2>
                        <p>Please <a href="/login">log in</a> or <a href="/register">register</a> to access store features.</p>
                    `;
}
})
    .catch(error => {
    console.error('Error fetching user data for menu:', error);
    const roleSpecificLinks = document.getElementById('role-specific-links');
    const authLinks = document.getElementById('auth-links');
    const contentDiv = document.getElementById('content');

    roleSpecificLinks.innerHTML = '';
    authLinks.innerHTML = `
                    <a href="/login">Login</a>
                    <a href="/register">Register</a>
                `;

    contentDiv.innerHTML = `
                    <h2>Welcome to the Online Store</h2>
                    <p>Please <a href="/login">log in</a> or <a href="/register">register</a> to access store features.</p>
                `;
});
});
