package no.dittnavn.footy.stats;

import no.dittnavn.footy.util.TeamNameNormalizer;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.TeamOutcome;
import no.dittnavn.footy.model.TeamProfile;
import no.dittnavn.footy.analysis.Team.TeamDNA;
import java.util.LinkedList;


import java.util.ArrayDeque;
import java.util.Deque;

public class TeamStats {

    private final String name;

    public int games;
    public int wins;
    public int draws;
    public int losses;
    public int goalsScored = 0;
    public int goalsConceded = 0;
    public int homeGames;
    public int homeWins;
    public int awayLosses;
    public int homeLosses;
    public int awayWins;
    public int homeDraws;
    public int awayDraws;
    public int awayGames;
    public int homeGoalsScored = 0;
    public int homeGoalsConceded = 0;
    public int awayGoalsScored = 0;
    public int awayGoalsConceded = 0;

    public int shotsOnTarget = 0;
    public int shotsOnTargetAgainst = 0;

    public int homeShotsOnTarget = 0;
    public int awayShotsOnTarget = 0;
    private double possession;
    private double cornersFor;
    private double cornersAgainst;

    // LAST MATCH FEATURES
    private double lastMatchShots;
    private double lastMatchShotsAgainst;

    private double lastMatchSOT;
    private double lastMatchSOTAgainst;

    private double lastMatchCorners;
    private double lastMatchCornersAgainst;

    private double totalShots = 0;
    private double totalShotsAgainst = 0;

    private double shotsPerMatch = 0;
    private double shotsAgainstPerMatch = 0;



    private double elo = 1500; // start rating

    private static final int FORM_WINDOW = 5;

    private Deque<TeamOutcome> recentHomeResults = new ArrayDeque<>();
    private Deque<TeamOutcome> recentAwayResults = new ArrayDeque<>();

    private TeamDNA dna = new TeamDNA();
    private TeamProfile profile = new TeamProfile();

    public TeamStats(String name) {
        this.name = TeamNameNormalizer.normalize(name).toLowerCase();
    }

    public void updateFromMatch(Match match){

        String home = TeamNameNormalizer.normalize(match.getHomeTeam()).toLowerCase();
        String away = TeamNameNormalizer.normalize(match.getAwayTeam()).toLowerCase();

        if(!home.equals(name) && !away.equals(name)){
            return;

        }

        boolean isHome = home.equals(name);

        int sot = isHome ? match.getHomeShotsTarget() : match.getAwayShotsTarget();
        int sotAgainst = isHome ? match.getAwayShotsTarget() : match.getHomeShotsTarget();

// 🔥 FIX

        if (sot == 0 && sotAgainst == 0) {
            // tell kampen, men ikke legg til shots
            games++;
            return;
        }





/*
        System.out.println(
                "HOME SOT: " + match.getHomeShotsTarget() +
                        " | AWAY SOT: " + match.getAwayShotsTarget()
        );

 */



        games++;

        if(home.equals(name)){


            homeGames++;

            goalsScored += match.getHomeGoals();
            goalsConceded += match.getAwayGoals();

            // 🔥 NYTT (viktig for Poisson)
            homeGoalsScored += match.getHomeGoals();
            homeGoalsConceded += match.getAwayGoals();

            shotsOnTarget += match.getHomeShotsTarget();
            shotsOnTargetAgainst += match.getAwayShotsTarget();
            homeShotsOnTarget += match.getHomeShotsTarget();

            totalShots += match.getHomeShots();
            totalShotsAgainst += match.getAwayShots();



            // SHOTS

            this.lastMatchShots = match.getHomeShots();
            this.lastMatchShotsAgainst = match.getAwayShots();

            this.lastMatchSOT = match.getHomeShotsTarget();
            this.lastMatchSOTAgainst = match.getAwayShotsTarget();

            this.lastMatchCorners = match.getHomeCorners();
            this.lastMatchCornersAgainst = match.getAwayCorners();
// SHOTS


            updateForm(match.getHomeGoals(), match.getAwayGoals(), true);

            if(match.getHomeGoals() > match.getAwayGoals()){
                wins++;
                homeWins++;
            } else if(match.getHomeGoals() == match.getAwayGoals()){
                draws++;
                homeDraws++;
            } else {
                losses++;
                homeLosses++;
            }

        } else if(away.equals(name)){

            this.lastMatchShots = match.getAwayShots();
            this.lastMatchShotsAgainst = match.getHomeShots();

            this.lastMatchSOT = match.getAwayShotsTarget();
            this.lastMatchSOTAgainst = match.getHomeShotsTarget();

            this.lastMatchCorners = match.getAwayCorners();
            this.lastMatchCornersAgainst = match.getHomeCorners();

            awayGames++;

            goalsScored += match.getAwayGoals();
            goalsConceded += match.getHomeGoals();

            // 🔥 NYTT (viktig for Poisson)
            awayGoalsScored += match.getAwayGoals();
            awayGoalsConceded += match.getHomeGoals();

            shotsOnTarget += match.getAwayShotsTarget();
            shotsOnTargetAgainst += match.getHomeShotsTarget();
            awayShotsOnTarget += match.getAwayShotsTarget();

            totalShots += match.getAwayShots();
            totalShotsAgainst += match.getHomeShots();

            updateForm(match.getAwayGoals(), match.getHomeGoals(), false);

            if(match.getAwayGoals() > match.getHomeGoals()){
                wins++;
                awayWins++;
            } else if(match.getAwayGoals() == match.getHomeGoals()){
                draws++;
                awayDraws++;
            } else {
                losses++;
                awayLosses++;
            }
        }

        /*
        if (games % 10 == 0) {
            System.out.println(
                    name +
                            " SOT=" + getShotsOnTargetPerMatch() +
                            " | Against=" + getShotsOnTargetAgainstPerMatch()
            );

        }

         */

        if (games > 0) {
            shotsPerMatch = totalShots / games;
            shotsAgainstPerMatch = totalShotsAgainst / games;
        }



    }


    private void updateForm(int scored, int conceded, boolean home){

        TeamOutcome o =
                scored > conceded ? TeamOutcome.WIN :
                        scored == conceded ? TeamOutcome.DRAW :
                                TeamOutcome.LOSS;

        // total form
        addResult(o);

        // home / away form
        if(home) addResult(recentHomeResults, o);
        else addResult(recentAwayResults, o);
    }


    private Deque<TeamOutcome> lastResults = new LinkedList<>();

    public void addResult(TeamOutcome outcome){
        lastResults.addFirst(outcome);

        if(lastResults.size() > 5){
            lastResults.removeLast();
        }
    }

    private void addResult(Deque<TeamOutcome> list, TeamOutcome outcome){
        list.addFirst(outcome);

        if(list.size() > 5){
            list.removeLast();
        }
    }



    public double getHomeFormScore(){ return calculate(recentHomeResults); }
    public double getAwayFormScore(){ return calculate(recentAwayResults); }

    private double calculate(Deque<TeamOutcome> r){
        if(r.isEmpty()) return 0.5;
        int pts = 0;
        for(TeamOutcome o : r){
            if(o == TeamOutcome.WIN) pts += 3;
            else if(o == TeamOutcome.DRAW) pts += 1;
        }
        return pts / (double)(r.size()*3);
    }

    public int getGames(){ return games; }
    public int getWins(){ return wins; }
    public int getDraws(){ return draws; }
    public int getLosses(){ return losses; }


    public TeamProfile getProfile(){ return profile; }
    public TeamDNA getDNA(){ return dna; }
    public String getName(){ return name; }

    public int getGoalsScored(){ return goalsScored; }
    public int getGoalsConceded(){ return goalsConceded; }

    @Override
    public String toString(){
        return name +
                " [games=" + games +
                ", wins=" + wins +
                ", draws=" + draws +
                ", losses=" + losses +
                ", elo=" + elo +
                "]";
    }
    public double getFormScore(){ // kan justere betydning av siste formutvikling

        if(lastResults.isEmpty()) return 0.5;

        double score = 0;
        double totalWeight = 0;

        double weight = 0.9; // bestemme hvor mye gamle kamper har å si, lavere weight dess mindre -
// betydning gamle kamper har å si, men motsatt dess større betydning har nyere kamper å si
        for(TeamOutcome r : lastResults){

            double points = 0;

            switch(r){
                case WIN: points = 3; break;
                case DRAW: points = 1; break;
                case LOSS: points = 0; break;
            }

            score += points * weight;
            totalWeight += 3 * weight;

            // nyere kamper teller mer
            weight *= 0.8;
        }

        return score / totalWeight;
    }

    // bergene og justere betydning av et lags utvikling basert på prestasjoner,
    public double getAdvancedFormScore(boolean isHome) {

        // =========================
        // BASE FORM
        // =========================

        double form = getFormScore();

        double venueForm =
                isHome
                        ? getHomeFormScore()
                        : getAwayFormScore();

        // =========================
        // SHOTS / PRESSURE
        // =========================

        double sotDiff =
                getShotsOnTargetPerMatch()
                        - getShotsOnTargetAgainstPerMatch();

        double shotDiff =
                getShotsPerMatch()
                        - getShotsAgainstPerMatch();

        double cornerDiff =
                getCornersFor()
                        - getCornersAgainst();

        // =========================
        // RECENT MOMENTUM
        // =========================

        double recentMomentum =
                (getLastMatchSOT() - getLastMatchSOTAgainst()) * 0.6 +
                        (getLastMatchShots() - getLastMatchShotsAgainst()) * 0.4;

        // =========================
        // GOAL MOMENTUM
        // =========================

        double goalDiff =
                getGoalsScoredPerMatch()
                        - getGoalsConcededPerMatch();

        // =========================
        // POSSESSION
        // =========================

        double possessionScore =
                (getPossession() - 50.0) / 50.0;

        // =========================
        // TEAM DNA
        // =========================

        double dnaScore =
                dna.getAttackIndex() * 0.30 +
                        dna.getDefenseIndex() * 0.30 +
                        dna.getTempoIndex() * 0.15 +
                        dna.getComebackAbility() * 0.15 -
                        dna.getChokeFactor() * 0.10;

        // =========================
        // TEAM PROFILE
        // =========================

        double profileScore =
                profile.getCoachRating() * 0.30 +
                        profile.getMotivation() * 0.30 -
                        profile.getFatigue() * 0.20 -
                        (profile.getInjuredPlayers() * 0.05) -
                        (profile.getSuspendedPlayers() * 0.05);

        // =========================
        // ELO
        // =========================

        double eloScore =
                (getElo() - 1500.0) / 1000.0;

        // =========================
        // NORMALIZATION
        // =========================

        sotDiff /= 5.0;
        shotDiff /= 10.0;
        cornerDiff /= 5.0;
        recentMomentum /= 5.0;
        goalDiff /= 3.0;

        // =========================
        // FINAL SCORE
        // =========================

        double score =

                form * 0.20 +
                        venueForm * 0.10 +

                        sotDiff * 0.15 +
                        shotDiff * 0.10 +
                        cornerDiff * 0.05 +

                        recentMomentum * 0.10 +
                        goalDiff * 0.10 +

                        possessionScore * 0.05 +

                        dnaScore * 0.05 +
                        profileScore * 0.05 +

                        eloScore * 0.05;

        // clamp
        if(score < 0) score = 0;
        if(score > 1) score = 1;

        return score;
    }

    // =========================
// HOME STATS
// =========================
    public int getHomeGames() {
        return homeGames;
    }

    public int getHomeWins() {
        return homeWins;
    }

    public int getHomeLosses() {
        return homeLosses;
    }

    public int getHomeDraws() {
        return homeDraws;
    }

    // =========================
// AWAY STATS
// =========================
    public int getAwayGames() {
        return awayGames;
    }

    public int getAwayWins() {
        return awayWins;
    }

    public int getAwayLosses() {
        return awayLosses;
    }

    public int getAwayDraws() {
        return awayDraws;
    }

    public double getGoalsScoredPerMatch(){
        if(games == 0) return 1.2; // fallback baseline
        return (double) goalsScored / games;
    }

    public double getGoalsConcededPerMatch(){
        if(games == 0) return 1.2;
        return (double) goalsConceded / games;
    }

    public void adjustElo(double delta) {
        this.elo += delta;

        if (this.elo < 800) this.elo = 800;
        if (this.elo > 2400) this.elo = 2400;
    }

    public double getElo() {
        return elo;
    }

    public void setElo(double elo) {
        this.elo = elo;
    }

    public void updateElo(double opponentElo, double score) {
        int k = 20;

        double expected =
                1.0 / (1.0 + Math.pow(10, (opponentElo - this.elo) / 400));

        this.elo += k * (score - expected);

        if (this.elo < 800) this.elo = 800;
        if (this.elo > 2400) this.elo = 2400;
    }

    public double getHomeGoalsScoredPerMatch(){
        if(homeGames == 0) return 1.4;
        return (double) homeGoalsScored / homeGames;
    }

    public double getHomeGoalsConcededPerMatch(){
        if(homeGames == 0) return 1.2;
        return (double) homeGoalsConceded / homeGames;
    }

    public double getAwayGoalsScoredPerMatch(){
        if(awayGames == 0) return 1.1;
        return (double) awayGoalsScored / awayGames;
    }

    public double getAwayGoalsConcededPerMatch(){
        if(awayGames == 0) return 1.4;
        return (double) awayGoalsConceded / awayGames;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public double getShotsOnTargetPerMatch() {
        if (games == 0) return 4.0;
        return (double) shotsOnTarget / games;
    }

    public double getShotsOnTargetAgainstPerMatch() {
        if (games == 0) return 4.0;
        return (double) shotsOnTargetAgainst / games;
    }


    public double getPossession() {
        return possession;
    }

    public double getCornersFor() {
        return cornersFor;
    }

    public double getCornersAgainst() {
        return cornersAgainst;
    }

    public double getLastMatchShots() {
        return lastMatchShots;
    }

    public double getLastMatchShotsAgainst() {
        return lastMatchShotsAgainst;
    }

    public double getLastMatchSOT() {
        return lastMatchSOT;
    }

    public double getLastMatchSOTAgainst() {
        return lastMatchSOTAgainst;
    }

    public double getLastMatchCorners() {
        return lastMatchCorners;
    }

    public double getLastMatchCornersAgainst() {
        return lastMatchCornersAgainst;
    }

    public void setLastMatchShots(double v) { lastMatchShots = v; }
    public void setLastMatchShotsAgainst(double v) { lastMatchShotsAgainst = v; }

    public void setLastMatchSOT(double v) { lastMatchSOT = v; }
    public void setLastMatchSOTAgainst(double v) { lastMatchSOTAgainst = v; }

    public void setLastMatchCorners(double v) { lastMatchCorners = v; }
    public void setLastMatchCornersAgainst(double v) { lastMatchCornersAgainst = v; }


    public double getShotsPerMatch() {
        return shotsPerMatch;
    }

    public double getShotsAgainstPerMatch() {
        return shotsAgainstPerMatch;
    }


}
