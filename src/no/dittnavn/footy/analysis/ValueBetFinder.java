package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.analysis.odds.OddsConverter;

public class ValueBetFinder {

    public static boolean isHomeValue(TeamStats home, TeamStats away, double homeOdds){

        double[] probs = ProbabilityEngine.matchProbabilities(home,away);
        double aiProb = probs[0];

        double bookProb = OddsConverter.impliedProbability(homeOdds);

        return aiProb > bookProb;
    }

}