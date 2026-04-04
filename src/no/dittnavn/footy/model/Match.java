package no.dittnavn.footy.model;

import no.dittnavn.footy.util.TeamNameNormalizer;

public class Match {

    private int id;

    private String date;
    private String league;

    private String homeTeam;
    private String awayTeam;

    private int homeGoals;
    private int awayGoals;

    private int homeShots;
    private int awayShots;

    private int homeShotsTarget;
    private int awayShotsTarget;

    private int homeCorners;
    private int awayCorners;

    private double homeOdds;
    private double drawOdds;
    private double awayOdds;


    private int homeYellow;
    private int awayYellow;

    private double psHome;
    private double psDraw;
    private double psAway;

    private double maxHome;
    private double maxDraw;
    private double maxAway;

    private double avgHome;
    private double avgDraw;
    private double avgAway;

    private double PSCH;
    private double PSCA;
    private double AvgCH;
    private double AvgCA;
    private double MaxCH;
    private double MaxCA;
    private int homeFouls = -1;
    private int awayFouls = -1;


    public Match() {
    }

    // =====================================================
    // KONSTRUKTØR MED ODDS (brukes av Historical loader)
    // =====================================================

    public Match(String date,
                 String homeTeam,
                 String awayTeam,
                 int homeGoals,
                 int awayGoals,
                 double homeOdds,
                 double drawOdds,
                 double awayOdds) {

        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.homeOdds = homeOdds;
        this.drawOdds = drawOdds;
        this.awayOdds = awayOdds;
    }

    // =====================================================
    // KONSTRUKTØR UTEN ODDS (brukes av CsvMatchLoader)
    // =====================================================

    public Match(String date,
                 String homeTeam,
                 String awayTeam,
                 int homeGoals,
                 int awayGoals) {

        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;

        // Default odds (hvis ikke tilgjengelig)
        this.homeOdds = 0.0;
        this.drawOdds = 0.0;
        this.awayOdds = 0.0;
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        this.league = league;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public double getHomeOdds() {
        return homeOdds;
    }

    public double getDrawOdds() {
        return drawOdds;
    }

    public double getAwayOdds() {
        return awayOdds;
    }

    // =====================================================
    // OUTCOME LOGIC
    // =====================================================

    public Outcome getOutcome() {
        if (homeGoals > awayGoals) return Outcome.HOME;
        if (homeGoals < awayGoals) return Outcome.AWAY;
        return Outcome.DRAW;
    }

    public TeamOutcome getOutcomeFor(String teamName) {

        String normalizedTeam = TeamNameNormalizer.normalize(teamName).toLowerCase();
        String home = TeamNameNormalizer.normalize(homeTeam).toLowerCase();
        String away = TeamNameNormalizer.normalize(awayTeam).toLowerCase();

        if (normalizedTeam.equals(home)) {

            if (homeGoals > awayGoals) return TeamOutcome.WIN;
            if (homeGoals == awayGoals) return TeamOutcome.DRAW;
            return TeamOutcome.LOSS;

        } else if (normalizedTeam.equals(away)) {

            if (awayGoals > homeGoals) return TeamOutcome.WIN;
            if (awayGoals == homeGoals) return TeamOutcome.DRAW;
            return TeamOutcome.LOSS;
        }

        throw new IllegalArgumentException("Laget deltok ikke i kampen: " + teamName);
    }

    // =====================================================
    // UNIQUE KEY
    // =====================================================

    public String getUniqueKey() {
        return date + "_" +
                TeamNameNormalizer.normalize(homeTeam) + "_" +
                TeamNameNormalizer.normalize(awayTeam);
    }

    // =====================================================
    // DEBUG
    // =====================================================

    @Override
    public String toString() {
        return "Match{" +
                "date='" + date + '\'' +
                ", league='" + league + '\'' +
                ", homeTeam='" + homeTeam + '\'' +
                ", awayTeam='" + awayTeam + '\'' +
                ", homeGoals=" + homeGoals +
                ", awayGoals=" + awayGoals +
                ", homeOdds=" + homeOdds +
                ", drawOdds=" + drawOdds +
                ", awayOdds=" + awayOdds +
                '}';
    }

    // =====================================================
// SETTERS FOR ODDS (brukes av CsvFixtureLoader)
// =====================================================

    public void setHomeOdds(double homeOdds) {
        this.homeOdds = homeOdds;
    }

    public void setDrawOdds(double drawOdds) {
        this.drawOdds = drawOdds;
    }

    public void setAwayOdds(double awayOdds) {
        this.awayOdds = awayOdds;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setHomeGoals(int homeGoals) {
        this.homeGoals = homeGoals;
    }

    public void setAwayGoals(int awayGoals) {
        this.awayGoals = awayGoals;
    }

    public void setHomeShots(int homeShots) {
        this.homeShots = homeShots;
    }

    public void setAwayShots(int awayShots) {
        this.awayShots = awayShots;
    }

    public void setHomeShotsTarget(int homeShotsTarget) {
        this.homeShotsTarget = homeShotsTarget;
    }

    public void setAwayShotsTarget(int awayShotsTarget) {
        this.awayShotsTarget = awayShotsTarget;
    }

    public void setHomeCorners(int homeCorners) {
        this.homeCorners = homeCorners;
    }

    public void setAwayCorners(int awayCorners) {
        this.awayCorners = awayCorners;
    }

    public int getHomeShots() {
        return homeShots;
    }


    public int getAwayShots() {
        return awayShots;
    }


    public int getHomeShotsTarget() {
        return homeShotsTarget;
    }


    public int getAwayShotsTarget() {
        return awayShotsTarget;
    }


    public int getHomeCorners() {
        return homeCorners;
    }


    public int getAwayCorners() {
        return awayCorners;
    }


    public int getHomeYellow() {
        return homeYellow;
    }

    public void setHomeYellow(int homeYellow) {
        this.homeYellow = homeYellow;
    }

    public int getAwayYellow() {
        return awayYellow;
    }

    public void setAwayYellow(int awayYellow) {
        this.awayYellow = awayYellow;
    }

    public double getPsHome() {
        return psHome;
    }

    public double getPsDraw() {
        return psDraw;
    }

    public double getPsAway() {
        return psAway;
    }

    public double getMaxHome() {
        return maxHome;
    }

    public double getMaxDraw() {
        return maxDraw;
    }

    public double getMaxAway() {
        return maxAway;
    }

    public double getAvgHome() {
        return avgHome;
    }

    public double getAvgDraw() {
        return avgDraw;
    }

    public double getAvgAway() {
        return avgAway;
    }

    public void setPsHome(double psHome) {
        this.psHome = psHome;
    }

    public void setPsDraw(double psDraw) {
        this.psDraw = psDraw;
    }

    public void setPsAway(double psAway) {
        this.psAway = psAway;
    }

    public void setMaxHome(double maxHome) {
        this.maxHome = maxHome;
    }

    public void setMaxDraw(double maxDraw) {
        this.maxDraw = maxDraw;
    }

    public void setMaxAway(double maxAway) {
        this.maxAway = maxAway;
    }

    public void setAvgHome(double avgHome) {
        this.avgHome = avgHome;
    }

    public void setAvgDraw(double avgDraw) {
        this.avgDraw = avgDraw;
    }

    public void setAvgAway(double avgAway) {
        this.avgAway = avgAway;
    }

    public double getPSCH() { return PSCH; }
    public double getPSCA() { return PSCA; }

    public double getAvgCH() { return AvgCH; }
    public double getAvgCA() { return AvgCA; }

    public double getMaxCH() { return MaxCH; }
    public double getMaxCA() { return MaxCA; }


    public void setHomeFouls(int v) { this.homeFouls = v; }
    public void setAwayFouls(int v) { this.awayFouls = v; }

    public int getHomeFouls() {
        return homeFouls;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAwayFouls() {
        return awayFouls;
    }
}