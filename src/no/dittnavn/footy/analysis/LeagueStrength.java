package no.dittnavn.footy.analysis;

public class LeagueStrength {

    public static double getStrength(String team){

        team = team.toLowerCase();

        // Skottland
        if(team.contains("celtic") || team.contains("rangers")
                || team.contains("hibernian") || team.contains("hearts"))
            return 0.65;

        // Bundesliga
        if(team.contains("stuttgart") || team.contains("bayern")
                || team.contains("dortmund") || team.contains("leipzig"))
            return 0.97;

        // Premier League
        if(team.contains("arsenal") || team.contains("chelsea")
                || team.contains("manchester") || team.contains("liverpool"))
            return 1.00;

        // Portugal
        if(team.contains("benfica") || team.contains("porto") || team.contains("sporting"))
            return 0.90;

        // default
        return 0.88;
    }
}
