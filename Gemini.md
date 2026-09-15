# Build an Offline Coffee Shop POS System — "Coffee ni Dawgz"

## PROJECT OVERVIEW

Build a complete, functional, offline Point-of-Sale (POS) system for a coffee shop named:

**COFFEE NI DAWGZ**

The application should be designed as a real-world coffee shop POS rather than a simple console-based program.

The system must work **completely offline** and must not require an internet connection for normal operation.

Use a **local SQLite database** to permanently store application data.

The application should have a cute, modern coffee-shop design featuring a **Golden Retriever mascot**, coffee elements, and subtle paw-print decorations while still remaining professional and easy for cashiers to use.

---

# 1. CORE REQUIREMENTS

The system must include:

* Offline operation
* SQLite local database
* Admin and Cashier accounts
* Login system
* Product management
* POS/order system
* Product customization
* Inventory management
* Low-stock alerts
* Cash payments
* GCash payments
* VAT calculation
* Discounts
* Automatic change calculation
* Order numbers
* Order status tracking
* Receipt generation
* Printable receipts
* Digitally saved receipts
* Sales reports
* Best-selling products
* Transaction history
* Dashboard
* Data persistence after restarting the application
* Backup/restore capability
* Error handling
* Input validation
* Role-based permissions

Do not create a fake/demo interface where buttons do nothing. All major features must actually work.

---

# 2. OFFLINE ARCHITECTURE

The application must be completely offline.

Use:

**SQLite** as the local database.

The application must not depend on:

* Internet
* Cloud databases
* Online APIs
* Firebase
* Supabase
* Online authentication
* Online payment verification
* Internet-based inventory systems

All data must be stored locally.

The following information must persist after closing and reopening the application:

* Users
* Products
* Prices
* Inventory
* Orders
* Transactions
* Discounts
* Sales history
* Settings

Use a clean database access layer so that database operations are separated from the UI.

Do not put all SQL queries directly inside UI event handlers.

---

# 3. USER AUTHENTICATION

Create a login screen.

Example:

COFFEE NI DAWGZ

Golden Retriever mascot

Username:
[________________]

Password:
[________________]

[ LOGIN ]

The system must support two roles:

## ADMIN

Admin can:

* View dashboard
* Manage products
* Add products
* Edit products
* Delete products
* Manage inventory
* Add stock
* Remove stock
* View low-stock products
* Manage cashiers
* Create cashier accounts
* Remove cashier accounts
* Reset cashier passwords
* View all transactions
* View sales reports
* View best-selling products
* Configure system settings
* Manage discounts
* View all sales history

## CASHIER

Cashiers can:

* Login
* Open POS
* Create orders
* Customize products
* Add products to cart
* Remove products from cart
* Change quantities
* Apply permitted discounts
* Accept cash payments
* Accept GCash payments
* Calculate change
* Complete transactions
* Print receipts
* Save receipts
* View current orders
* View their own transaction history

Cashiers must NOT be able to:

* Delete products
* Change product prices
* Modify system settings
* Manage employees
* Create admin accounts
* Delete transaction records
* Modify inventory directly unless specifically allowed by the system design

Use secure password handling appropriate for a local application.

---

# 4. PRODUCT CATEGORIES

Create these main categories:

## COFFEE

Include products such as:

* Americano
* Latte
* Cappuccino
* Mocha
* Spanish Latte

## NON-COFFEE

Include products such as:

* Chocolate
* Matcha
* Milk-based drinks

## ICED DRINKS

Include products such as:

* Iced Latte
* Iced Mocha
* Iced Americano

## HOT DRINKS

Include products such as:

* Hot Latte
* Hot Chocolate
* Hot Americano

## SNACKS

Include products such as:

* Cookies
* Brownies
* Cake
* Sandwiches

## ADD-ONS / TOPPINGS

Include examples such as:

* Extra shot
* Extra syrup
* Whipped cream
* Extra milk

Make the product system configurable so Admin can add, edit, or delete products later.

---

# 5. PRODUCT SIZES

Drinks must support three sizes:

* Small
* Medium
* Large

Each size can have its own price.

Example:

Iced Latte

Small — ₱90
Medium — ₱110
Large — ₱130

The Admin must be able to configure the prices.

---

# 6. PRODUCT CUSTOMIZATION

Drinks should support:

## Temperature

* Hot
* Iced

## Sugar Level

* 0%
* 25%
* 50%
* 75%
* 100%

## Milk Options

Examples:

* Fresh Milk
* Full Cream
* Other configured milk options

Customization options must NOT automatically add additional charges.

Do not implement extra pricing for sugar level, milk selection, or Hot/Iced selection unless explicitly configured later by Admin.

---

# 7. POS SCREEN

Create a modern POS interface.

The cashier should be able to:

1. Select a category
2. Select a product
3. Select size
4. Select Hot/Iced if applicable
5. Select sugar level
6. Select milk option
7. Select add-ons/toppings
8. Add the customized product to the cart
9. Change quantity
10. Remove items
11. See subtotal
12. Apply discount
13. Calculate VAT
14. See final total
15. Select payment method
16. Complete transaction

Use a visual product grid rather than requiring the cashier to type product names manually.

---

# 8. SHOPPING CART

The cart should clearly display:

* Product name
* Size
* Temperature
* Sugar level
* Milk option
* Add-ons
* Quantity
* Unit price
* Item total

Example:

Iced Latte
Large
50% Sugar
Fresh Milk
Extra Caramel

Qty: 2

₱130 × 2 = ₱260

The cashier must be able to edit or remove an item before payment.

---

# 9. ORDER NUMBERS

Automatically generate unique order numbers.

Example:

ORDER #000001
ORDER #000002
ORDER #000003

Do not duplicate order numbers.

Store the order number in the database.

---

# 10. ORDER STATUS

Orders must support:

* Pending
* Preparing
* Completed
* Cancelled

The cashier should be able to see the current order status.

Cancelled orders must not accidentally reduce inventory or be counted as completed sales.

If inventory has already been deducted, properly restore the inventory when appropriate.

---

# 11. PAYMENT SYSTEM

Support only:

## CASH

When the customer pays cash:

Example:

Total:
₱397.60

Cash received:
₱500.00

Change:
₱102.40

The system must automatically calculate the change.

Do not allow the cashier to complete the transaction if the cash received is less than the total.

Display a clear validation message.

## GCASH

Because the application is offline, do NOT attempt to verify GCash automatically.

Instead:

1. Display the total amount.
2. Display the configured GCash payment information.
3. Cashier manually verifies that payment has been received.
4. Cashier clicks "Payment Received".
5. Complete the transaction.

Allow Admin to configure the GCash account/payment information shown by the POS.

---

# 12. VAT

Apply **12% VAT**.

VAT must be added to the subtotal.

Example:

Subtotal:
₱100.00

VAT 12%:
₱12.00

Total:
₱112.00

Display VAT clearly on the POS and receipt.

Do not hard-code the VAT calculation throughout the application.

Store the VAT rate in system settings so Admin can modify it if necessary.

---

# 13. DISCOUNTS

Support:

* Senior Citizen discount
* PWD discount
* Student discount
* Promotional discount
* Custom discount

Design the discount system so that Admin can configure discount rules.

The system must clearly show:

Subtotal
Discount
VAT
Final Total

Make sure the calculation order is consistent and configurable where appropriate.

Do not silently apply discounts.

The cashier must select the applicable discount before completing payment.

---

# 14. INVENTORY

Use **product-level inventory**, not ingredient-level inventory.

Example:

Iced Latte
Stock: 25

Americano
Stock: 40

Chocolate Cake
Stock: 8

When an order is completed:

Iced Latte stock:
25 → 24

The system should prevent selling products when stock reaches zero.

Display:

OUT OF STOCK

when appropriate.

---

# 15. LOW-STOCK ALERTS

Create a configurable low-stock threshold.

Example:

Threshold:
5

If inventory reaches 5 or below:

⚠ LOW STOCK

If inventory reaches zero:

🔴 OUT OF STOCK

Admin dashboard should show low-stock products.

---

# 16. INVENTORY MANAGEMENT

Admin must be able to:

* View current stock
* Add stock
* Remove stock
* Adjust inventory
* View stock status
* Set low-stock threshold

Record inventory adjustments where possible.

Do not allow inventory to become negative.

---

# 17. RECEIPTS

Every completed transaction must generate a receipt.

The receipt must be:

1. Displayed on screen
2. Printable
3. Saveable digitally

Example structure:

================================
COFFEE NI DAWGZ
"Brewed for Good Dawgz"
=======================

Order #: 000123
Cashier: Juan
Date: 08/16/2026
Time: 06:15 PM

---

Iced Latte
Large × 2                 ₱260.00

Sugar: 50%
Milk: Fresh Milk

Chocolate Cake × 1         ₱95.00

---

Subtotal:                 ₱355.00
Discount:                   ₱0.00
VAT (12%):                 ₱42.60
---------------------------------

TOTAL:                    ₱397.60

Payment: CASH
Cash:                     ₱500.00
Change:                   ₱102.40

================================
THANK YOU, DAWG! 🐾
===================

Receipt information should be saved with the transaction.

---

# 18. SALES DASHBOARD

Create an Admin dashboard showing:

* Today's sales
* Number of transactions
* Items sold
* Best-selling product
* Low-stock products
* Recent transactions

Example:

COFFEE NI DAWGZ
ADMIN DASHBOARD

Today's Sales
₱8,450.00

Transactions
73

Items Sold
142

Best Seller
Iced Latte

Low Stock
3 Products

Use cards, charts, and tables where appropriate.

---

# 19. SALES REPORTS

Create:

## Daily Report

Show:

* Total sales
* Number of transactions
* Total discounts
* Total VAT
* Cash sales
* GCash sales
* Best-selling products

## Weekly Report

Show the same information grouped by day.

## Monthly Report

Show:

* Total monthly sales
* Total transactions
* Sales by day
* Best-selling products
* Payment method breakdown

Allow Admin to select date ranges where appropriate.

---

# 20. BEST-SELLING PRODUCTS

Calculate best-selling products based on completed transactions.

Show:

Rank
Product
Quantity Sold
Total Revenue

Example:

1. Iced Latte — 42 sold
2. Spanish Latte — 35 sold
3. Americano — 29 sold

Cancelled transactions must not count toward best-selling statistics.

---

# 21. TRANSACTION HISTORY

Create a transaction history screen.

Display:

* Order number
* Date
* Time
* Cashier
* Total
* Payment method
* Order status

Allow Admin to select a transaction and view its full details.

Do not allow completed transactions to be casually deleted.

Use proper transaction records for auditing.

---

# 22. EMPLOYEE MANAGEMENT

Admin can:

* Create cashier
* Edit cashier
* Disable cashier
* Delete cashier where appropriate
* Reset cashier password

Do not allow a cashier to access Admin functions.

---

# 23. DATABASE DESIGN

Use SQLite.

Create an organized relational database.

Suggested tables:

### users

* id
* username
* password_hash
* role
* full_name
* status
* created_at

### categories

* id
* name
* status

### products

* id
* category_id
* name
* description
* small_price
* medium_price
* large_price
* stock_quantity
* low_stock_threshold
* status
* created_at
* updated_at

### orders

* id
* order_number
* cashier_id
* subtotal
* discount
* vat
* total
* payment_method
* amount_received
* change_amount
* status
* created_at

### order_items

* id
* order_id
* product_id
* product_name
* size
* temperature
* sugar_level
* milk_option
* quantity
* unit_price
* total_price

### add_ons

* id
* name
* price
* status

### order_item_addons

* id
* order_item_id
* addon_id
* addon_name
* price

### inventory_logs

* id
* product_id
* quantity_changed
* previous_quantity
* new_quantity
* reason
* user_id
* created_at

### discounts

* id
* name
* type
* value
* status

### settings

* id
* setting_key
* setting_value

Use foreign keys and proper database constraints.

Do not store duplicate information unnecessarily.

---

# 24. DATA VALIDATION

The system must validate all user inputs.

Examples:

* Product price cannot be negative.
* Stock cannot be negative.
* Quantity must be greater than zero.
* Cash payment cannot be less than total.
* Username cannot be empty.
* Password cannot be empty.
* Product name cannot be empty.
* Required fields cannot be blank.
* Duplicate usernames should be prevented.
* Duplicate product names should be handled appropriately.

Display user-friendly error messages.

Do not allow the application to crash because of invalid input.

---

# 25. ERROR HANDLING

Implement proper error handling.

The application should gracefully handle:

* Database errors
* Invalid input
* Missing data
* Duplicate records
* Failed transactions
* Printing errors
* File saving errors

Do not expose raw stack traces to normal users.

Log technical errors appropriately for debugging.

---

# 26. BACKUP AND RESTORE

Because this is an offline application, data backup is important.

Provide Admin with:

**Backup Database**

and

**Restore Database**

The backup should contain:

* Products
* Inventory
* Users
* Orders
* Transactions
* Settings
* Reports/history

Do not overwrite existing data without confirmation.

Ask for confirmation before restoring a database.

---

# 27. USER INTERFACE

Create a modern desktop POS interface.

The UI should be:

* Clean
* Fast
* Easy to understand
* Beginner-friendly
* Responsive
* Professional
* Cute but not childish

Use a coffee-inspired visual identity.

Main visual concept:

**Golden Retriever + Coffee**

Use:

* Golden Retriever mascot
* Paw prints
* Coffee cup elements
* Rounded cards/buttons
* Clean typography
* Coffee-shop inspired interface
* Clear icons
* Consistent spacing

The mascot should be prominent on the login screen and used more subtly throughout the application.

Do not allow decorative elements to interfere with the POS workflow.

---

# 28. MAIN APPLICATION SCREENS

Create these screens:

## Login

* Golden Retriever mascot
* Username
* Password
* Login button

## Cashier POS

* Category navigation
* Product grid
* Cart
* Product customization
* Order number
* Discount
* VAT
* Payment
* Receipt

## Order Status

* Pending
* Preparing
* Completed
* Cancelled

## Admin Dashboard

* Sales statistics
* Transaction statistics
* Best sellers
* Low stock

## Product Management

* Add
* Edit
* Delete
* Search
* Filter

## Inventory

* Stock list
* Add stock
* Remove stock
* Low-stock alerts

## Employee Management

* Cashiers
* Add cashier
* Edit cashier
* Disable cashier
* Reset password

## Reports

* Daily
* Weekly
* Monthly
* Date range

## Transactions

* Transaction history
* Transaction details

## Settings

* Shop information
* VAT
* GCash information
* Receipt settings
* Low-stock threshold

## Backup / Restore

* Backup database
* Restore database

---

# 29. SEARCH AND FILTERING

Add search functionality where useful.

Admin should be able to search:

* Products
* Transactions
* Employees

Allow filtering transactions by:

* Date
* Cashier
* Payment method
* Status

---

# 30. SOFTWARE ARCHITECTURE

Use a clean architecture.

Separate:

* UI
* Models
* Database access
* Business logic
* Services
* Utilities

Do not put all application logic into one class.

Use appropriate object-oriented programming principles.

Suggested structure:

src/
├── models/
├── dao/
├── services/
├── ui/
├── database/
├── utils/
└── Main.java

Use meaningful class names.

Keep methods reasonably small and readable.

Avoid unnecessary duplication.

---

# 31. IMPORTANT POS WORKFLOW

The complete cashier workflow should be:

Login
↓
Open POS
↓
Select category
↓
Select product
↓
Select size
↓
Select customization
↓
Add to cart
↓
Review cart
↓
Apply discount if applicable
↓
Calculate subtotal
↓
Calculate 12% VAT
↓
Calculate final total
↓
Select Cash or GCash
↓
If Cash:
Enter amount received
Calculate change
↓
If GCash:
Display GCash payment information
Cashier manually verifies payment
↓
Complete transaction
↓
Deduct inventory
↓
Save transaction
↓
Generate order number/receipt
↓
Display receipt
↓
Allow print
↓
Allow save
↓
Return to POS

The database transaction should ensure that an order is not partially saved.

---

# 32. IMPORTANT INVENTORY WORKFLOW

Only deduct inventory when the transaction is successfully completed.

Example:

Before:

Iced Latte = 20

Customer buys 2.

After successful payment:

Iced Latte = 18

If the order is cancelled before completion:

Iced Latte = 20

If inventory has already been deducted and an authorized cancellation occurs afterward, restore the appropriate quantity.

Never allow negative inventory.

---

# 33. SECURITY AND DATA INTEGRITY

Implement:

* Password hashing
* Role-based authorization
* Prepared SQL statements
* Database constraints
* Input validation
* Confirmation dialogs for destructive actions
* Proper transaction handling

Never concatenate raw user input directly into SQL queries.

---

# 34. DEFAULT DATA

On the first launch, initialize the database with sample products so the application can immediately be tested.

Include sample categories and products.

Also create an initial Admin account and a sample Cashier account.

Clearly document the default login credentials in the project documentation and require changing default credentials after initial setup if appropriate.

---

# 35. TESTING

Test the following scenarios:

### Successful Cash Sale

Product → Cart → Discount → VAT → Cash → Change → Receipt → Inventory deduction.

### Successful GCash Sale

Product → Cart → VAT → GCash → Manual confirmation → Receipt → Inventory deduction.

### Insufficient Cash

Total = ₱200

Cash = ₱150

System must reject the payment.

### Out of Stock

Stock = 0

Product cannot be added to an order.

### Low Stock

Stock reaches threshold.

System displays a low-stock warning.

### Cancelled Order

Cancelled order should not be counted as completed sales.

### Restart Application

Close the application and reopen it.

All saved data must still exist.

### Admin/Cashier Permissions

Verify that Cashier cannot access Admin-only functions.

### Backup/Restore

Create backup → modify data → restore backup → verify data.

---

# 36. IMPORTANT DEVELOPMENT RULES

Do not create unnecessary features that were not requested.

Do not replace SQLite with an online database.

Do not require internet access.

Do not create fake buttons.

Do not leave TODO placeholders for major features.

Do not hard-code transaction data.

Do not hard-code inventory.

Do not hard-code reports.

Do not hard-code users.

Use the database for persistent information.

Use reusable components.

Keep the application maintainable.

---

# 37. DOCUMENTATION

Provide a README explaining:

* Project purpose
* Features
* Technology stack
* How to install
* How to run
* Database setup
* Default login credentials
* Project structure
* How to create a backup
* How to restore a backup
* How to add products
* How inventory works
* How VAT works
* How GCash payments work offline

Also explain the important design decisions.

---

# FINAL GOAL

The final application should feel like a real small coffee-shop POS system called:

# 🐶☕ COFFEE NI DAWGZ

It should be:

**Offline + Reliable + Easy to Use + Cute + Professional**

The Golden Retriever theme should give the application its identity, but the POS workflow should remain efficient enough for a real cashier.

Prioritize:

1. Correct functionality
2. Data integrity
3. Offline operation
4. Easy cashier workflow
5. Clean architecture
6. Good UI/UX
7. Maintainability

Build the system incrementally and verify that each major feature works before moving to the next one.
