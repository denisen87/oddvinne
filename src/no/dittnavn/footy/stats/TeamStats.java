package no.dittnavn.footy.stats;

import no.dittnavn.footy.util.TeamNameNormalizer;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.TeamOutcome;
import no.dittnavn.footy.model.TeamProfile;
import no.dittnavn.footy.analysis.team.TeamDNA;
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

            awayGames++;

            goalsScored += match.getAwayGoals();
            goalsConceded += match.getHomeGoals();

            // 🔥 NYTT (viktig for Poisson)
            awayGoalsScored += match.getAwayGoals();
            awayGoalsConceded += match.getHomeGoals();

            shotsOnTarget += match.getAwayShotsTarget();
            shotsOnTargetAgainst += match.getHomeShotsTarget();
            awayShotsOnTarget += match.getAwayShotsTarget();

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

        if (games % 10 == 0) {
            System.out.println(
                    name +
                            " SOT=" + getShotsOnTargetPerMatch() +
                            " | Against=" + getShotsOnTargetAgainstPerMatch()
            );
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
    public double getFormScore(){

        if(lastResults.isEmpty()) return 1.5;

        double score = 0;

        for(TeamOutcome r : lastResults){
            switch(r){
                case WIN: score += 3; break;
                case DRAW: score += 1; break;
                case LOSS: score += 0; break;
            }
        }

        return score / lastResults.size();
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



}
