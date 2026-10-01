# SETUP GUIDE

## A. What you need in VS Code

### Required VS Code extensions
1. Extension Pack for Java
2. Live Server
3. Flutter
4. Dart

You do NOT need to install a separate "Java feature" inside VS Code. You need:
- JDK installed on Windows
- Java Extension Pack in VS Code

For Flutter, the Flutter and Dart extensions are useful, but the Flutter SDK itself must also be installed.

## B. XAMPP
1. Open XAMPP Control Panel.
2. Start MySQL.
3. Open http://localhost/phpmyadmin
4. Click Import.
5. Select database/shop.sql.
6. Click Go.

The script creates:
- shopping_db
- users
- categories
- products
- orders
- order_items

## C. MySQL Connector/J
Download the official MySQL Connector/J JAR.
Put it here:

backend/lib/mysql-connector-j.jar

The filename can be different, but update the commands below.

## D. Java backend

Open a terminal inside backend.

Windows PowerShell example:

javac -cp "lib/mysql-connector-j.jar" -d out src/main/java/com/shop/*.java

Then:

java -cp "out;lib/mysql-connector-j.jar" com.shop.Main

If your JAR has another name, replace it in both commands.

The backend runs on:
http://localhost:8080

## E. Website

Open frontend/index.html with Live Server.

The JavaScript uses:
http://localhost:8080/api

## F. Flutter app

Open flutter_app as a separate VS Code folder/workspace.

Run:

flutter pub get

Then connect an Android phone with USB debugging enabled OR use an Android emulator.

Run:

flutter run

### Very important for a physical Android phone
Do NOT use localhost in the Flutter app because localhost means the phone itself.

Change API_BASE in flutter_app/lib/main.dart to your laptop's local IPv4 address, for example:

http://192.168.1.10:8080/api

Your phone and laptop must be on the same Wi-Fi network, and Windows Firewall may need to allow Java on port 8080.

## G. Project flow

Login/Register
    ↓
Categories
    ↓
Products
    ↓
Cart
    ↓
Checkout
    ↓
Payment mode
    ↓
Order created
    ↓
Expected delivery = +7 days
    ↓
Cancel available for first 2 days
    ↓
Delivered / Cancelled

## H. Suggested college demonstration

Use these demo accounts:
- Create your own account through Register.
- Add products to cart.
- Choose COD.
- Place an order.
- Show the order date and expected delivery date.
- Show cancellation before the 2-day deadline.

For a real project, add admin login, stock management, product images, search, order history, and real payment integration only after the basic system works.
