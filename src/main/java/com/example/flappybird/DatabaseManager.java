package com.example.flappybird;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/flappy_bird";
    private static final String USER = "root";
    private static final String PASSWORD = "Paulo168+=";

    public static void saveScoreAndUsername(String username, int score) {
        String insertPlayer = "INSERT IGNORE into Players (username)VALUES (?)";
        String insertScore = "INSERT into Scores (username, score)VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)){

            PreparedStatement stmt1 = connection.prepareStatement(insertPlayer);
            stmt1.setString(1, username);
            stmt1.executeUpdate();

            PreparedStatement stmt2 = connection.prepareStatement(insertScore);
            stmt2.setString(1, username);
            stmt2.setInt(2, score);
            stmt2.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getTopThreeScore() {
        ArrayList<String> topScores = new ArrayList<>();
        String query = """
                SELECT username, MAX(score) AS max_score
                FROM Scores
                GROUP BY username
                ORDER BY max_score DESC
                LIMIT 3
                """;
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)){
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while(rs.next()) {
                String userName = rs.getString("username");
                int maxScore = rs.getInt("max_score");
                topScores.add(userName + ": " + maxScore);
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }
        return topScores;
    }
}