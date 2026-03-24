package no.dittnavn.footy.analysis.learning;

import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;

import java.util.List;

public class AutoDataTrainer {

    private final StatsIndeks indeks;

    public AutoDataTrainer(StatsIndeks indeks) {
        this.indeks = indeks;
    }

    public void trainFromMatches(List<Match> matches){

        for(Match match : matches){

            // hent eller opprett lag
            TeamStats home = indeks.getOrCreate(match.getHomeTeam());
            TeamStats away = indeks.getOrCreate(match.getAwayTeam());

            // oppdater stats
            home.updateFromMatch(match);
            away.updateFromMatch(match);

            // ELO
            double homeScore =
                    match.getHomeGoals() > match.getAwayGoals() ? 1 :
                            match.getHomeGoals() == match.getAwayGoals() ? 0.5 : 0;

            double awayScore = 1 - homeScore;

            double homeElo = home.getElo();
            double awayElo = away.getElo();

// forventet resultat
            double expectedHome = 1.0 / (1.0 + Math.pow(10, (awayElo - homeElo) / 400));
            double expectedAway = 1.0 / (1.0 + Math.pow(10, (homeElo - awayElo) / 400));

// K-factor
            int k = 20;

// juster Elo
            home.adjustElo(k * (homeScore - expectedHome));
            away.adjustElo(k * (awayScore - expectedAway));
        }

        System.out.println("Auto-training ferdig: " + matches.size() + " kamper.");
    }
}