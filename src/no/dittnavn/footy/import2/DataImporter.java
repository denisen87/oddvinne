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

        Map<String, String> leagues = Map.of(
                "E0", "premierleague",
                "D1", "bundesliga",
                "I1", "seriea",
                "SP1", "laliga",
                "F1", "ligue1",
                "NOR", "eliteserien",
                "SP2", "laliga2",
                "E2", "league one",
                "N1", "Eredivisie",
                "ENL", "National League"
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