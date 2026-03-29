package no.dittnavn.footy.import2;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.loader.FootballDataLoader;
import no.dittnavn.footy.db.DatabaseManager;


public class DataImporter {

    public static void main(String[] args) {

        DatabaseManager.init();

        importFootballData();   // 👈 start importen her
    }

    private static void importFootballData() {

        Map<String, String> leagues = Map.ofEntries(
                Map.entry("E0", "premierleague"),
                Map.entry("D1", "bundesliga"),
                Map.entry("I1", "seriea"),
                Map.entry("SP1", "laliga"),
                Map.entry("F1", "ligue1"),
                Map.entry("NOR", "eliteserien"),
                Map.entry("SP2", "laliga2"),
                Map.entry("E2", "league one"),
                Map.entry("N1", "Eredivisie"),
                Map.entry("ENL", "National League"),
                Map.entry("ARG", "Primera LPF"),
                Map.entry("SC1", "Championship"),
                Map.entry("EL2", "League two"),
                Map.entry("BRA", "Brasil Serie A")
        );

        try (Connection conn = DatabaseManager.getConnection()) {

            conn.setAutoCommit(false);

            for (String code : leagues.keySet()) {

                String leagueName = leagues.get(code);
                File file = new File("data/" + code + ".csv");

                if (!file.exists()) {
                    System.out.println("Fant ikke fil: " + file.getAbsolutePath());
                    continue;
                }

                List<Match> matches = FootballDataLoader.load(file.getPath(), leagueName);

                System.out.println("Loaded (" + leagueName + "): " + matches.size());

                for (Match m : matches) {
                    m.setLeague(leagueName);
                    DatabaseManager.saveHistoricalMatch(conn, m);
                }

                System.out.println("Import ferdig: " + leagueName);
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}