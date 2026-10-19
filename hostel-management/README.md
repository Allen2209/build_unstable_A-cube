# Hostel Management System (Spring Boot MVC + MySQL)

## Requirements
- JDK 17 or above
- Apache Maven
- MySQL Server 8 (running on port 3306)

## Run
1. Open `src/main/resources/application.properties` and set your MySQL username and password.
2. In a terminal inside this folder:
   mvn spring-boot:run
3. Open http://localhost:8080

The database `hosteldb` and all tables are created automatically.

## Default logins (created on first start)
| Role    | Email              | Password   |
|---------|--------------------|------------|
| Admin   | admin@hostel.com   | admin123   |
| Student | student@hostel.com | student123 |

## View the stored data in MySQL
    mysql -u root -p
    USE hosteldb;
    SHOW TABLES;
    SELECT id, name, email, role FROM users;
