# Project “Hvězda”

“Hvězda” is a semester‑long web shop implementation designed to showcase core *e‑commerce business logic* and *robust data integrity:*
- **User Registration:**
Customers can sign up by providing personal details, including an optional date of birth.
- Order Creation & Stock Management:
When placing an order, the system checks inventory levels and automatically caps the requested quantity at the available stock.
- **Age‑Restricted Sales:**
For products with a legal age limit, the platform verifies the user’s age. If no birth date is provided or the user does not meet the age requirement, the order is rejected with an error.
- **Advanced Deletion Logic:**
Administrators may delete a user (and their loyalty card) only if the user has no active orders (status “processing” or “shipped”). If active orders exist, the operation returns false and no deletion occurs.

This project is implemented using Spring Boot (backend), with a user‑friendly GUI, and demonstrates my ability to translate complex requirements into a maintainable, reliable application.
