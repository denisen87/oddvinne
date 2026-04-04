package no.dittnavn.footy.db;

import java.sql.*;
import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.util.TeamNameNormalizer;
import java.util.List;
import java.util.ArrayList;
import no.dittnavn.footy.model.Match;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:betting.db";

    public static Connection getConnection() throws SQLException {
        return connect();
    }

    // 🔵 CONNECT
    private static Connection connection;

    public static Connection connect() throws SQLException {

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);

            Statement stmt = connection.createStatement();
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
            stmt.execute("PRAGMA busy_timeout=5000;");
            System.out.println("DB path: " + new java.io.File("betting.db").getAbsolutePath());
        }

        return connection;
    }

    // 🔵 OPPRETTER TABELL
    public static void init() {

        try {

            Connection conn = connect();
            Statement stmt = conn.createStatement();

            stmt.executeUpdate("""
CREATE TABLE IF NOT EXISTS historical_matches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    date TEXT,
    league TEXT,
    homeTeam TEXT,
    awayTeam TEXT,

    fthg INTEGER,
    ftag INTEGER,

    homeShots INTEGER,
    awayShots INTEGER,

    homeShotsTarget INTEGER,
    awayShotsTarget INTEGER,

    homeCorners INTEGER,
    awayCorners INTEGER,

    homeYellow INTEGER,
    awayYellow INTEGER,
    
    homeFouls INTEGER,
    awayFouls INTEGER,

    homeOdds REAL,
    drawOdds REAL,
    awayOdds REAL,

    psHome REAL,
    psDraw REAL,
    psAway REAL,

    maxHome REAL,
    maxDraw REAL,
    maxAway REAL,

    avgHome REAL,
    avgDraw REAL,
    avgAway REAL,
    UNIQUE(date, homeTeam, awayTeam, league)
)
""");

            stmt.executeUpdate("""
CREATE TABLE IF NOT EXISTS predictions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    date TEXT,
    homeTeam TEXT,
    awayTeam TEXT,

    poissonHome REAL,
    poissonDraw REAL,
    poissonAway REAL,

    eloHome REAL,
    eloDraw REAL,
    eloAway REAL,

    neuralHome REAL,
    neuralDraw REAL,
    neuralAway REAL,

    oddsHome REAL,
    oddsDraw REAL,
    oddsAway REAL,

    ensembleHome REAL,
    ensembleDraw REAL,
    ensembleAway REAL,

    modelConfidence REAL,

    valueHome REAL,
    valueDraw REAL,
    valueAway REAL,

    bet TEXT,
    stake REAL,

    result TEXT,
    profit REAL,

    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(date, homeTeam, awayTeam)
)
""");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 LAGRE PREDIKSJON (returnerer DB id)
    public static synchronized int savePrediction(MatchRecord r) {

        String sql = """
INSERT INTO predictions (
    date, homeTeam, awayTeam,
    poissonHome, poissonDraw, poissonAway,
    eloHome, eloDraw, eloAway,
    neuralHome, neuralDraw, neuralAway,
    oddsHome, oddsDraw, oddsAway,
    ensembleHome, ensembleDraw, ensembleAway,
    modelConfidence,
    valueHome, valueDraw, valueAway,
    bet, stake
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT(date, homeTeam, awayTeam)
DO UPDATE SET
    poissonHome = excluded.poissonHome,
    poissonDraw = excluded.poissonDraw,
    poissonAway = excluded.poissonAway,
    eloHome = excluded.eloHome,
    eloDraw = excluded.eloDraw,
    eloAway = excluded.eloAway,
    neuralHome = excluded.neuralHome,
    neuralDraw = excluded.neuralDraw,
    neuralAway = excluded.neuralAway,
    oddsHome = excluded.oddsHome,
    oddsDraw = excluded.oddsDraw,
    oddsAway = excluded.oddsAway,
    ensembleHome = excluded.ensembleHome,
    ensembleDraw = excluded.ensembleDraw,
    ensembleAway = excluded.ensembleAway,
    modelConfidence = excluded.modelConfidence,
    valueHome = excluded.valueHome,
    valueDraw = excluded.valueDraw,
    valueAway = excluded.valueAway,
    bet = excluded.bet,
    stake = excluded.stake
""";

        try (PreparedStatement ps = connect().prepareStatement(sql)) {

            int i = 1;

            // BASIC INFO
            ps.setString(i++, java.time.LocalDate.now().toString());
            ps.setString(i++, TeamNameNormalizer.normalize(r.homeTeam));
            ps.setString(i++, TeamNameNormalizer.normalize(r.awayTeam));

            // POISSON (midlertidig predicted)
            ps.setDouble(i++, r.predictedHome);
            ps.setDouble(i++, r.predictedDraw);
            ps.setDouble(i++, r.predictedAway);

            // ELO
            ps.setDouble(i++, r.predictedHome);
            ps.setDouble(i++, r.predictedDraw);
            ps.setDouble(i++, r.predictedAway);

            // NEURAL
            ps.setDouble(i++, r.predictedHome);
            ps.setDouble(i++, r.predictedDraw);
            ps.setDouble(i++, r.predictedAway);

            // ODDS
            ps.setDouble(i++, r.oddsHome);
            ps.setDouble(i++, r.oddsDraw);
            ps.setDouble(i++, r.oddsAway);

            // ENSEMBLE
            ps.setDouble(i++, r.predictedHome);
            ps.setDouble(i++, r.predictedDraw);
            ps.setDouble(i++, r.predictedAway);

            // CONFIDENCE
            ps.setDouble(i++, r.getConfidence());

            // VALUE (0 inntil du lagrer ekte)
            ps.setDouble(i++, r.valueHome);
            ps.setDouble(i++, r.valueDraw);
            ps.setDouble(i++, r.valueAway);

            // BET + STAKE
            ps.setString(i++, r.getBet());
            ps.setDouble(i++, r.stake);


            ps.executeUpdate();

            // 🔥 hent id fra DB
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // 🔵 OPPDATER RESULTAT ETTER KAMP
    public static synchronized void updateResult(int id, String result, double profit) {

        String sql = """
            UPDATE predictions
            SET result = ?, profit = ?
            WHERE id = ?
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, result);
            pstmt.setDouble(2, profit);
            pstmt.setInt(3, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printROIStats() {

        String sql = """
        SELECT bet, result, profit
        FROM predictions
        WHERE result IS NOT NULL
    """;

        int totalBets = 0;
        int wins = 0;
        int losses = 0;
        double totalProfit = 0;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                totalBets++;

                double profit = rs.getDouble("profit");
                totalProfit += profit;

                if (profit > 0) {
                    wins++;
                } else {
                    losses++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (totalBets == 0) {
            System.out.println("Ingen bets registrert enda.");
            return;
        }

        double winRate = (wins * 100.0) / totalBets;
        double roi = (totalProfit / totalBets) * 100;

        System.out.println("\n=== ROI ANALYSE ===");
        System.out.println("Totale bets: " + totalBets);
        System.out.println("Wins: " + wins);
        System.out.println("Losses: " + losses);
        System.out.printf("Winrate: %.2f%%\n", winRate);

        System.out.printf("\nTotal profit: %.2f units\n", totalProfit);
        System.out.printf("ROI: %.2f%%\n", roi);
    }

    public static void printROIByBetType() {

        String sql = """
        SELECT bet, profit
        FROM predictions
        WHERE result IS NOT NULL
    """;

        int homeBets = 0, drawBets = 0, awayBets = 0;
        double homeProfit = 0, drawProfit = 0, awayProfit = 0;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String bet = rs.getString("bet");
                double profit = rs.getDouble("profit");

                if ("HOME".equals(bet)) {
                    homeBets++;
                    homeProfit += profit;
                }
                else if ("DRAW".equals(bet)) {
                    drawBets++;
                    drawProfit += profit;
                }
                else if ("AWAY".equals(bet)) {
                    awayBets++;
                    awayProfit += profit;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("\n=== ROI PER BET-TYPE ===");

        if(homeBets > 0){
            System.out.println("\nHOME:");
            System.out.println("Bets: " + homeBets);
            System.out.printf("ROI: %.2f%%\n", (homeProfit / homeBets) * 100);
        }

        if(drawBets > 0){
            System.out.println("\nDRAW:");
            System.out.println("Bets: " + drawBets);
            System.out.printf("ROI: %.2f%%\n", (drawProfit / drawBets) * 100);
        }

        if(awayBets > 0){
            System.out.println("\nAWAY:");
            System.out.println("Bets: " + awayBets);
            System.out.printf("ROI: %.2f%%\n", (awayProfit / awayBets) * 100);
        }
    }

    public static void saveHistoricalMatch(Connection conn, Match m) {



        String sql = """
INSERT INTO historical_matches(
    date, league, homeTeam, awayTeam,
    fthg, ftag,
    homeShots, awayShots,
    homeShotsTarget, awayShotsTarget,
    homeCorners, awayCorners,
    homeYellow, awayYellow,
    homeOdds, drawOdds, awayOdds,
    psHome, psDraw, psAway,
    maxHome, maxDraw, maxAway,
    avgHome, avgDraw, avgAway,homeFouls, awayFouls
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT(date, homeTeam, awayTeam, league)
DO UPDATE SET
    homeOdds = excluded.homeOdds,
    drawOdds = excluded.drawOdds,
    awayOdds = excluded.awayOdds,
    psHome = excluded.psHome,
    psDraw = excluded.psDraw,
    psAway = excluded.psAway,
    maxHome = excluded.maxHome,
    maxDraw = excluded.maxDraw,
    maxAway = excluded.maxAway,
    avgHome = excluded.avgHome,
    avgDraw = excluded.avgDraw,
    avgAway = excluded.avgAway
""";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            if (m.getDate() == null || m.getDate().isBlank()) {
                System.out.println("SKIPPER kamp uten dato: " + m.getHomeTeam() + " vs " + m.getAwayTeam());
                return;
            }

            LocalDate parsed;
            String rawDate = m.getDate();

            try {
                parsed = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e1) {
                try {
                    parsed = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                } catch (Exception e2) {
                    System.out.println("FEIL DATO: " + rawDate);
                    return; // ✅ riktig her
                }
            }

            ps.setString(i++, parsed.toString());
            ps.setString(i++, m.getLeague());
            ps.setString(i++, TeamNameNormalizer.normalize(m.getHomeTeam()));
            ps.setString(i++, TeamNameNormalizer.normalize(m.getAwayTeam()));

            ps.setInt(i++, m.getHomeGoals());
            ps.setInt(i++, m.getAwayGoals());

            ps.setInt(i++, m.getHomeShots());
            ps.setInt(i++, m.getAwayShots());

            ps.setInt(i++, m.getHomeShotsTarget());
            ps.setInt(i++, m.getAwayShotsTarget());

            ps.setInt(i++, m.getHomeCorners());
            ps.setInt(i++, m.getAwayCorners());

            ps.setInt(i++, m.getHomeYellow());
            ps.setInt(i++, m.getAwayYellow());

            ps.setDouble(i++, m.getHomeOdds());
            ps.setDouble(i++, m.getDrawOdds());
            ps.setDouble(i++, m.getAwayOdds());

            ps.setDouble(i++, m.getPsHome());
            ps.setDouble(i++, m.getPsDraw());
            ps.setDouble(i++, m.getPsAway());

            ps.setDouble(i++, m.getMaxHome());
            ps.setDouble(i++, m.getMaxDraw());
            ps.setDouble(i++, m.getMaxAway());

            ps.setDouble(i++, m.getAvgHome());
            ps.setDouble(i++, m.getAvgDraw());
            ps.setDouble(i++, m.getAvgAway());

            ps.setInt(i++, m.getHomeFouls());
            ps.setInt(i++, m.getAwayFouls());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static List<Match> getHistoricalMatchesOrdered() {

        List<Match> list = new ArrayList<>();

        String sql = """
    SELECT date, homeTeam, awayTeam,
           fthg, ftag,
           homeShots, awayShots,
           homeShotsTarget, awayShotsTarget,
           homeOdds, drawOdds, awayOdds
    FROM historical_matches
    ORDER BY date ASC
    """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Match m = new Match(
                        rs.getString("date"),
                        rs.getString("homeTeam"),
                        rs.getString("awayTeam"),
                        rs.getInt("fthg"),
                        rs.getInt("ftag")
                );

                // shots
                m.setHomeShots(rs.getInt("homeShots"));
                m.setAwayShots(rs.getInt("awayShots"));

                // 🔥 SOT (med null-sjekk)
                int hSOT = rs.getInt("homeShotsTarget");
                if (rs.wasNull()) hSOT = -1;

                int aSOT = rs.getInt("awayShotsTarget");
                if (rs.wasNull()) aSOT = -1;

                m.setHomeShotsTarget(hSOT);
                m.setAwayShotsTarget(aSOT);

                // 🔥 ODDS (KRITISK FIX)
                double homeOdds = rs.getDouble("homeOdds");
                if (rs.wasNull() || homeOdds <= 1.01) homeOdds = -1;

                double drawOdds = rs.getDouble("drawOdds");
                if (rs.wasNull() || drawOdds <= 1.01) drawOdds = -1;

                double awayOdds = rs.getDouble("awayOdds");
                if (rs.wasNull() || awayOdds <= 1.01) awayOdds = -1;

                m.setHomeOdds(homeOdds);
                m.setDrawOdds(drawOdds);
                m.setAwayOdds(awayOdds);

                // 🔥 DEBUG (VIKTIG)
                System.out.println(
                        "DB -> SOT: " + hSOT + "|" + aSOT +
                                " | Odds: " + homeOdds + "/" + drawOdds + "/" + awayOdds
                );
                System.out.println(
                        "DB CHECK -> odds: " +
                                m.getHomeOdds() + " / " +
                                m.getDrawOdds() + " / " +
                                m.getAwayOdds()
                );

                list.add(m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("TOTAL MATCHES LOADED: " + list.size());

        return list;
    }
/*
    public static List<Match> getAllMatches() {

        List<Match> matches = new ArrayList<>();

        String sql = "SELECT * FROM matches"; // ⚠️ sjekk tabellnavn!

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Match m = new Match();

                m.setHomeTeam(rs.getString("home_team"));
                m.setAwayTeam(rs.getString("away_team"));

                m.setHomeGoals(rs.getInt("home_goals"));
                m.setAwayGoals(rs.getInt("away_goals"));

                // hvis du har odds:
                m.setHomeOdds(rs.getDouble("home_odds"));
                m.setDrawOdds(rs.getDouble("draw_odds"));
                m.setAwayOdds(rs.getDouble("away_odds"));

                matches.add(m);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return matches;
    }

 */
}