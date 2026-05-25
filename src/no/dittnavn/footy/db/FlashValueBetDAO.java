package no.dittnavn.footy.db;

import no.dittnavn.footy.db.FlashValueBetRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class FlashValueBetDAO {

    private static final String URL =
            "jdbc:sqlite:betting.db";

    public static void insert(
            FlashValueBetRecord match
    ) {

        String sql =
                "INSERT OR IGNORE INTO flashscore_valuebets " +
                        "(home_team, away_team, " +
                        "home_odds, draw_odds, away_odds, " +
                        "bet_type, value, match_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (

                Connection conn =
                        DriverManager.getConnection(URL);

                PreparedStatement ps =
                        conn.prepareStatement(sql)

        ) {

            ps.setString(1, match.homeTeam);

            ps.setString(2, match.awayTeam);

            ps.setDouble(3, match.homeOdds);

            ps.setDouble(4, match.drawOdds);

            ps.setDouble(5, match.awayOdds);

            ps.setString(6, match.betType);

            ps.setDouble(7, match.value);

            ps.setString(8, match.matchDate);

            ps.executeUpdate();

            System.out.println(
                    "FLASH INSERT: "
                            + match.homeTeam
                            + " vs "
                            + match.awayTeam
            );

        } catch(Exception e){

            e.printStackTrace();
        }
    }

    public static void createTable() {

        String sql =
                "CREATE TABLE IF NOT EXISTS flashscore_valuebets (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "home_team TEXT," +
                        "away_team TEXT," +

                        "home_odds REAL," +
                        "draw_odds REAL," +
                        "away_odds REAL," +

                        "bet_type TEXT," +
                        "value REAL," +
                        "match_date TEXT," +
                        "timestamp TEXT," +

                        "UNIQUE(home_team, away_team)" +
                        ")";

        try (

                Connection conn =
                        DriverManager.getConnection(URL);

                Statement stmt =
                        conn.createStatement()

        ) {

            stmt.execute(sql);

            System.out.println(
                    "flashscore_valuebets klar."
            );

        } catch(Exception e){

            e.printStackTrace();
        }
    }
}