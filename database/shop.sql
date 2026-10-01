CREATE DATABASE IF NOT EXISTS shopping_db;
USE shopping_db;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500),
    stock INT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    payment_mode ENUM('COD','UPI','CARD') NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    expected_delivery DATE NOT NULL,
    status ENUM('PLACED','SHIPPED','OUT_FOR_DELIVERY','DELIVERED','CANCELLED') DEFAULT 'PLACED',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

INSERT INTO categories (name) VALUES
('Women'),
('Men'),
('Electronics'),
('Beauty'),
('Home & Kitchen'),
('Footwear'),
('Jewels');

INSERT INTO products (category_id, name, description, price, image_url, stock) VALUES
(1,'Cotton Kurti','Comfortable everyday cotton kurti',499.00,'https://via.placeholder.com/300x220?text=Kurti',20),
(1,'Casual Top','Simple casual top',349.00,'https://via.placeholder.com/300x220?text=Top',25),
(2,'Casual Shirt','Regular fit casual shirt',599.00,'https://via.placeholder.com/300x220?text=Shirt',15),
(2,'Denim Jeans','Classic denim jeans',899.00,'https://via.placeholder.com/300x220?text=Jeans',12),
(3,'Wireless Mouse','2.4 GHz wireless mouse',399.00,'https://via.placeholder.com/300x220?text=Mouse',30),
(3,'Bluetooth Speaker','Portable Bluetooth speaker',999.00,'https://via.placeholder.com/300x220?text=Speaker',10),
(4,'Face Wash','Gentle daily face wash',249.00,'https://via.placeholder.com/300x220?text=Face+Wash',18),
(5,'Water Bottle','Reusable water bottle',299.00,'https://via.placeholder.com/300x220?text=Bottle',40),
(6,'Sneakers','Casual sneakers',1199.00,'https://via.placeholder.com/300x220?text=Sneakers',14),
(1,'Frocks','party waer',300.00,'https://via.placeholder.com/300x220?text=FROCKS',80),
(7,'pearls chain','party wear',100.00,'https://via.placeholder.com/300x220?text=chain',9);
