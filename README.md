# Online Shopping System — Java + HTML + MySQL + Flutter

This is a beginner-friendly college project starter.

## Technologies
- Java: REST-style HTTP backend
- HTML/CSS/JavaScript: web frontend
- MySQL: database
- XAMPP: MySQL + phpMyAdmin
- Flutter/Dart: Android mobile app
- VS Code: coding

## Main features
1. User registration and login
2. Product categories
3. Product listing
4. Product details
5. Add to cart
6. Place order
7. Payment mode selection (COD / UPI / Card — demo only)
8. Order status
9. Expected delivery date = order date + 7 days
10. Cancellation allowed until 2 days after ordering
11. My Orders page
12. MySQL database

## Important
This project does NOT process real payments. Payment modes are only stored as a demo selection.
Do not enter real card/UPI information.

## Project structure
- backend/       Java API
- frontend/      HTML/CSS/JS website
- database/      MySQL database script
- flutter_app/   Flutter Android client
- SETUP.md       detailed setup steps

## Quick start
1. Install JDK 17 or newer.
2. Install VS Code.
3. Install Java Extension Pack in VS Code.
4. Install XAMPP and start MySQL.
5. Import database/shop.sql into phpMyAdmin.
6. Download MySQL Connector/J and place the JAR in backend/lib/.
7. Edit DB username/password in DBConnection.java if needed.
8. Compile and run the Java backend.
9. Open frontend/index.html using VS Code Live Server, or serve the frontend through a local server.
10. For Flutter, install Flutter SDK and Android SDK, then run the Flutter app on a phone/emulator.

Default backend:
http://localhost:8080
