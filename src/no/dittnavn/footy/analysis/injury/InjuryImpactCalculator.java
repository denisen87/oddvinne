package no.dittnavn.footy.analysis.injury;

import no.dittnavn.footy.model.TeamProfile;

public class InjuryImpactCalculator {

    public double calculateImpact(TeamProfile profile){

        int injured = profile.getInjuredPlayers();
        int suspended = profile.getSuspendedPlayers();

        double coach = profile.getCoachRating();
        double motivation = profile.getMotivation();

        // base skadeeffekt
        double injuryLoad = injured * 0.03 + suspended * 0.025;

        // dårlig coach forsterker skadeeffekt
        injuryLoad *= (1.2 - coach);

        // høy motivasjon demper skadeeffekt
        injuryLoad *= (1.1 - motivation);

        return injuryLoad;
    }
}
