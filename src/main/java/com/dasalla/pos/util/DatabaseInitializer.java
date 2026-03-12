package com.dasalla.pos.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN','CASHIER','STAFF')),
                    full_name TEXT NOT NULL,
                    created_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);

            // Customers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    created_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);

            // Orders table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_number TEXT NOT NULL UNIQUE,
                    customer_id INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK(status IN ('PENDING','PROCESSING','COMPLETED','CLAIMED')),
                    total_amount REAL NOT NULL DEFAULT 0,
                    payment_method TEXT CHECK(payment_method IN ('CASH','GCASH')),
                    payment_status TEXT NOT NULL DEFAULT 'UNPAID'
                        CHECK(payment_status IN ('UNPAID','PAID')),
                    gcash_reference TEXT,
                    notes TEXT,
                    created_at TEXT DEFAULT (datetime('now','localtime')),
                    updated_at TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (customer_id) REFERENCES customers(id)
                )
            """);

            // Order items table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    service_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    quantity REAL NOT NULL DEFAULT 1,
                    unit_price REAL NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(id)
                )
            """);

            // Transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    payment_method TEXT NOT NULL,
                    gcash_reference TEXT,
                    processed_by INTEGER,
                    created_at TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (order_id) REFERENCES orders(id),
                    FOREIGN KEY (processed_by) REFERENCES users(id)
                )
            """);

            // Inventory table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT NOT NULL UNIQUE,
                    quantity INTEGER NOT NULL DEFAULT 0,
                    unit TEXT NOT NULL DEFAULT 'bottles',
                    restock_threshold INTEGER NOT NULL DEFAULT 10,
                    unit_cost REAL NOT NULL DEFAULT 0,
                    updated_at TEXT DEFAULT (datetime('now','localtime'))
                )
            """);

            // Seed default admin user (password: admin123)
            stmt.execute("""
                INSERT OR IGNORE INTO users (username, password, role, full_name)
                VALUES ('admin', 'admin123', 'ADMIN', 'System Administrator')
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO users (username, password, role, full_name)
                VALUES ('cashier', 'cashier123', 'CASHIER', 'Default Cashier')
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO users (username, password, role, full_name)
                VALUES ('staff', 'staff123', 'STAFF', 'Default Staff')
            """);

            // Seed default inventory
            stmt.execute("""
                INSERT OR IGNORE INTO inventory (item_name, quantity, unit, restock_threshold, unit_cost)
                VALUES ('Detergent', 30, 'bottles', 10, 85.00)
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO inventory (item_name, quantity, unit, restock_threshold, unit_cost)
                VALUES ('Fabric Softener', 25, 'bottles', 8, 120.00)
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO inventory (item_name, quantity, unit, restock_threshold, unit_cost)
                VALUES ('Bleach', 15, 'bottles', 5, 65.00)
            """);
            stmt.execute("""
                INSERT OR IGNORE INTO inventory (item_name, quantity, unit, restock_threshold, unit_cost)
                VALUES ('Dryer Sheets', 50, 'pcs', 20, 5.00)
            """);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
