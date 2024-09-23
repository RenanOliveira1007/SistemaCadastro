package com.mycompany.sistemacadastro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:derby://localhost:1527/bd_cliente"; 
    private static final String USERNAME = "root"; 
    private static final String PASSWORD = "812663"; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
