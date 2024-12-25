function showToast(message, duration = 3000) {
    const toast = document.getElementById('toast');
    if (!toast) return;

    toast.textContent = message;
    toast.className = 'toast show';
    setTimeout(() => {
        toast.className = toast.className.replace('show', '');
    }, duration);
}

async function setupMenu() {
    try {
        const response = await fetch('http://localhost:8081/users/authenticated', { credentials: 'include' });
        const roleSpecificLinks = document.getElementById('role-specific-links');
        const authLinks = document.getElementById('auth-links');

        if (response.ok) {
            const user = await response.json();

            if (user.role === 'ADMINISTRATOR') {
                roleSpecificLinks.innerHTML = `
                    <a href="/users">Manage Users</a>
                    <a href="/bonus-cards-admin">Manage Bonus Cards</a>
                    <a href="/products-admin">Manage Products</a>
                    <a href="/orders-admin">Manage Orders</a>
                `;
            } else if (user.role === 'CUSTOMER') {
                roleSpecificLinks.innerHTML = `
                    <a href="/products-customer">Products</a>
                    <a href="/cart">Shopping Cart</a>
                    <a href="/personal-details">My Details</a>
                `;
            } else {
                roleSpecificLinks.innerHTML = '';
            }

            authLinks.innerHTML = `
                <form action="http://localhost:8081/logout" method="POST" style="margin: 0;">
                    <button type="submit" class="btn btn-secondary">Logout</button>
                </form>
            `;

            handleAccessControl(user.role);
        } else {
            throw new Error("Failed to fetch user data");
        }
    } catch (error) {
        console.error(error);
        const roleSpecificLinks = document.getElementById('role-specific-links');
        const authLinks = document.getElementById('auth-links');
        roleSpecificLinks.innerHTML = '';

        authLinks.innerHTML = `
            <a href="http://localhost:8082/login" class="btn btn-secondary">Login</a>
            <a href="http://localhost:8082/register" class="btn btn-primary">Register</a>
        `;

        handleAccessControl(null);
    }
}

function handleAccessControl(userRole) {
    const path = window.location.pathname.toLowerCase();

    const adminPages = [
        '/users',
        '/bonus-cards-admin',
        '/products-admin',
        '/orders-admin'
    ];

    const customerPages = [
        '/cart',
        '/products-customer',
        '/personal-details'
    ];

    const isAdminPage = adminPages.some(adminPage => path.startsWith(adminPage));
    const isCustomerPage = customerPages.some(customerPage => path.startsWith(customerPage));

    if (isAdminPage) {
        if (userRole !== 'ADMINISTRATOR') {
            if (userRole === null) {
                showToast('Please log in to access admin pages.', 3000);
                window.location.href = '/login';
            } else {
                showToast('Access denied: You do not have permission to view this page.', 5000);
                const content = document.getElementById('content');
                if (content) {
                    content.innerHTML = `
                        <h1>Access Denied</h1>
                        <p>You do not have permission to view this page.</p>
                    `;
                }
            }
        }
    }

    if (isCustomerPage) {
        if (userRole !== 'CUSTOMER') {
            if (userRole === null) {
                showToast('Please log in to access customer pages.', 3000);
                window.location.href = '/login';
            } else {
                showToast('Access denied: You do not have permission to view this page.', 5000);
                const content = document.getElementById('content');
                if (content) {
                    content.innerHTML = `
                        <h1>Access Denied</h1>
                        <p>You do not have permission to view this page.</p>
                    `;
                }
            }
        }
    }
}