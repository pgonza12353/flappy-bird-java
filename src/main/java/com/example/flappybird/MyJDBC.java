package com.example.flappybird;

import java.sql.*;

public class MyJDBC {
    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/flappy_bird",
                    "root",
                    "Paulo168+="
            );
            // TESTING DATABASE CONNECTION
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT VERSION()");
            if (resultSet.next()) {
                System.out.println("Connected to MySQL, version: " + resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            connection.close();
        }catch(SQLException e) {
            e.printStackTrace();
        }

    }
}
