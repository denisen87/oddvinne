package no.dittnavn.footy.integration.sportradar;

import no.dittnavn.footy.model.Match;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class MatchStatsDAO {

    private final Connection connection;

    public MatchStatsDAO(Connection connection) {

        this.connection = connection;

        createTable();
    }

    private void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS match_stats (
                
                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    home_team TEXT,
                    away_team TEXT,

                    home_shots INTEGER,
                    away_shots INTEGER,

                    home_shots_target INTEGER,
                    away_shots_target INTEGER,

                    home_corners INTEGER,
                    away_corners INTEGER
                )
                """;

        try {

            Statement stmt =
                    connection.createStatement();

            stmt.execute(sql);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void save(Match match) {

        String sql = """
                INSERT INTO match_stats (
                
                    home_team,
                    away_team,

                    home_shots,
                    away_shots,

                    home_shots_target,
                    away_shots_target,

                    home_corners,
                    away_corners
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {

            PreparedStatement ps =
                    connection.prepareStatement(sql);

            ps.setString(1, match.getHomeTeam());
            ps.setString(2, match.getAwayTeam());

            ps.setInt(3, match.getHomeShots());
            ps.setInt(4, match.getAwayShots());

            ps.setInt(5, match.getHomeShotsTarget());
            ps.setInt(6, match.getAwayShotsTarget());

            ps.setInt(7, match.getHomeCorners());
            ps.setInt(8, match.getAwayCorners());

            ps.executeUpdate();

            System.out.println(
                    "Saved match stats"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}