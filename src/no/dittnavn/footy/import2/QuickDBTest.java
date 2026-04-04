import java.sql.*;

public class QuickDBTest {

    public static void main(String[] args) {

        String url = "jdbc:sqlite:C:/Users/denis/IdeaProjects/oddvinne/betting.db";

        String sql = "SELECT homeTeam, awayTeam, fthg, ftag FROM historical_matches LIMIT 5";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("=== DATA FRA DB ===");

            while (rs.next()) {
                System.out.println(
                        rs.getString("homeTeam") + " vs " +
                                rs.getString("awayTeam") +
                                " | " +
                                rs.getInt("fthg") + "-" +
                                rs.getInt("ftag")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}