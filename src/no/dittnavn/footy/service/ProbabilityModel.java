package no.dittnavn.footy.service;

import no.dittnavn.footy.model.MatchOdds;

public class ProbabilityModel {

    public double homeProb(MatchOdds m) {
        return 0.33; // midlertidig
    }

    public double drawProb(MatchOdds m) {
        return 0.33;
    }

    public double awayProb(MatchOdds m) {
        return 0.33;
    }
}