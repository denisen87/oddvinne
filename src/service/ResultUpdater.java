package service;

import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.integration.FlashscoreResultFetcher;
import no.dittnavn.footy.integration.FlashscoreResultFetcher.MatchResult;
import no.dittnavn.footy.util.TeamNameNormalizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ResultUpdater {

    private static final double DEFAULT_STAKE = 100.0;

    public static void updateResults() {

        System.out.println("=== RESULT UPDATE START ===");

        try (Connection conn = DatabaseManager.connect()) {

            // 🔹 hent predictions uten resultat
            String pendingSql =
                    "SELECT * FROM predictions WHERE result IS NULL";

            PreparedStatement ps = conn.prepareStatement(pendingSql);
            ResultSet rs = ps.executeQuery();

            // 🔹 hent ferdige kamper fra Flashscore
            List<String> leagues = List.of("europa-league");

            List<MatchResult> results = new ArrayList<>();

            System.out.println("Flashscore results hentet: " + results.size());

            // 🔹 lag map for rask lookup
            Map<String, MatchResult> resultMap = new HashMap<>();

            for (MatchResult r : results) {

                String home = TeamNameNormalizer.normalize(r.homeTeam);
                String away = TeamNameNormalizer.normalize(r.awayTeam);

                String key = home + "_" + away + "_" + r.date;

                resultMap.put(key, r);
            }

            int updated = 0;

            // 🔹 loop predictions
            while (rs.next()) {

                int id = rs.getInt("id");

                String dbHome =
                        TeamNameNormalizer.normalize(rs.getString("homeTeam"));

                String dbAway =
                        TeamNameNormalizer.normalize(rs.getString("awayTeam"));

                String dbDate = rs.getString("date");

                String bet = rs.getString("bet");

                double oddsHome = rs.getDouble("oddsHome");
                double oddsDraw = rs.getDouble("oddsDraw");
                double oddsAway = rs.getDouble("oddsAway");

                double stake = rs.getDouble("stake");
                if (stake <= 0) stake = DEFAULT_STAKE;

                String key = dbHome + "_" + dbAway + "_" + dbDate;

                MatchResult r = resultMap.get(key);

                if (r == null) continue;

                String actualResult;

                if (r.homeGoals > r.awayGoals)
                    actualResult = "HOME";
                else if (r.homeGoals < r.awayGoals)
                    actualResult = "AWAY";
                else
                    actualResult = "DRAW";

                double profit = calculateProfit(
                        bet,
                        actualResult,
                        stake,
                        oddsHome,
                        oddsDraw,
                        oddsAway
                );

                PreparedStatement update =
                        conn.prepareStatement(
                                "UPDATE predictions SET result=?, profit=? WHERE id=?"
                        );

                update.setString(1, actualResult);
                update.setDouble(2, profit);
                update.setInt(3, id);

                update.executeUpdate();

                updated++;

                System.out.println(
                        "UPDATED: "
                                + dbHome + " vs " + dbAway
                                + " -> " + actualResult
                                + " profit=" + profit
                );
            }

            System.out.println("Oppdaterte kamper: " + updated);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=== RESULT UPDATE FERDIG ===");
    }

    // 🔹 profit-kalkyle
    private static double calculateProfit(
            String bet,
            String result,
            double stake,
            double oddsHome,
            double oddsDraw,
            double oddsAway
    ) {

        if (bet == null) return 0;

        bet = bet.toUpperCase();
        result = result.toUpperCase();

        if (bet.equals(result)) {

            if (result.equals("HOME"))
                return stake * (oddsHome - 1);

            if (result.equals("DRAW"))
                return stake * (oddsDraw - 1);

            if (result.equals("AWAY"))
                return stake * (oddsAway - 1);
        }

        return -stake;
    }
}