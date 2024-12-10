**SETUP Database with this SQL code**

CREATE DATABASE ChatApp;
USE ChatApp;

CREATE TABLE Users (
id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50) UNIQUE NOT NULL,
password VARCHAR(50) NOT NULL
);

INSERT INTO Users (username, password) VALUES ('user1', 'pass1'), ('user2', 'pass2');

**PORT**

port is configured to be 3306

username: root
password: 12345678