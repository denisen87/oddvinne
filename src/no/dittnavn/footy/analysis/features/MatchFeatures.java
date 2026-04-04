package no.dittnavn.footy.analysis.features;

import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.model.TeamProfile;
import no.dittnavn.footy.analysis.Team.TeamDNA;

public class MatchFeatures {

    // lagreferanser (viktig for flip og AI)
    private TeamStats homeStats;
    private TeamStats awayStats;

    // core forskjeller
    public double ratingDiff;
    public double formDiff;
    public double winrateDiff;
    public double goalDiff;

    // kontekst
    public double motivationDiff;
    public double fatigueDiff;
    public double coachDiff;
    public double injuryDiff;

    // odds
    public double oddsHome;
    public double oddsDraw;
    public double oddsAway;

    // DNA
    public double attackDiff;
    public double defenseDiff;
    public double tempoDiff;
    public double comebackDiff;
    public double chokeDiff;

    public double shotDiff;
    public double sotDiff;
    public double cornerDiff;


    // 🔥 HOVED KONSTRUKTØR
    public MatchFeatures(TeamStats home, TeamStats away,
                         double oddsHome, double oddsDraw, double oddsAway) {


        this.homeStats = home;
        this.awayStats = away;

        // rating
        this.ratingDiff = home.getElo() - away.getElo();

        // form
        this.formDiff = home.getHomeFormScore() - away.getAwayFormScore();

        // winrate
        this.winrateDiff =
                (home.getWins() / (double)Math.max(1, home.getGames()))
                        -
                        (away.getWins() / (double)Math.max(1, away.getGames()));

        // målbalanse
        this.goalDiff =
                (home.getGoalsScored() - home.getGoalsConceded())
                        -
                        (away.getGoalsScored() - away.getGoalsConceded());

        // team profiler
        TeamProfile hp = home.getProfile();
        TeamProfile ap = away.getProfile();

        this.motivationDiff = hp.getMotivation() - ap.getMotivation();
        this.fatigueDiff = hp.getFatigue() - ap.getFatigue();
        this.coachDiff = hp.getCoachRating() - ap.getCoachRating();
        this.injuryDiff = hp.getInjuredPlayers() - ap.getInjuredPlayers();

        // odds
        this.oddsHome = oddsHome;
        this.oddsDraw = oddsDraw;
        this.oddsAway = oddsAway;

        // DNA
        TeamDNA homeDNA = home.getDNA();
        TeamDNA awayDNA = away.getDNA();

        this.attackDiff = homeDNA.getAttackIndex() - awayDNA.getAttackIndex();
        this.defenseDiff = homeDNA.getDefenseIndex() - awayDNA.getDefenseIndex();
        this.tempoDiff = homeDNA.getTempoIndex() - awayDNA.getTempoIndex();
        this.comebackDiff = homeDNA.getComebackAbility() - awayDNA.getComebackAbility();
        this.chokeDiff = homeDNA.getChokeFactor() - awayDNA.getChokeFactor();
    }


    // 🔥 FLIP (brukes for å doble treningsdata)
    public MatchFeatures flip() {

        return new MatchFeatures(
                awayStats,
                homeStats,
                oddsAway,
                oddsDraw,
                oddsHome
        );
    }


    // getters (valgfritt men nyttig senere)
    public TeamStats getHomeStats() { return homeStats; }
    public TeamStats getAwayStats() { return awayStats; }

    public void setMatchStats(int homeShots, int awayShots,
                              int homeSOT, int awaySOT,
                              int homeCorners, int awayCorners) {

        this.shotDiff = safeDiff(homeShots, awayShots);
        this.sotDiff = safeDiff(homeSOT, awaySOT);
        this.cornerDiff = safeDiff(homeCorners, awayCorners);
    }

    private double safeDiff(int a, int b) {
        if (a < 0 || b < 0) return 0;
        return a - b;
    }



}
