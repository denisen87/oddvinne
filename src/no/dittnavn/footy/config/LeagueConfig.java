package no.dittnavn.footy.config;

import java.util.Map;

public class LeagueConfig { //

    private static final Map<String, String> BASE_URLS =
            Map.ofEntries(

                    Map.entry(
                            "NOR",
                            "https://www.flashscore.com/football/norway/eliteserien/"
                    ),

                    Map.entry(
                            "SWE",
                            "https://www.flashscore.com/football/sweden/allsvenskan/"
                    ),

                    Map.entry(
                            "BRA",
                            "https://www.flashscore.com/football/brazil/serie-a-betano/"
                    ),

                    Map.entry(
                            "IRE",
                            "https://www.flashscore.com/football/ireland/premier-division/"
                    ),

                    Map.entry(
                            "FIN",
                            "https://www.flashscore.com/football/finland/suomen-cup/"
                    ),

                    Map.entry(
                            "E0",
                            "https://www.flashscore.com/football/england/premier-league/"
                    ),

                    Map.entry(
                            "SP1",
                            "https://www.flashscore.com/football/spain/laliga/"
                    ),

                    Map.entry(
                            "D1",
                            "https://www.flashscore.com/football/germany/bundesliga/"
                    ),

                    Map.entry(
                            "I1",
                            "https://www.flashscore.com/football/italy/serie-a/"
                    ),

                    Map.entry(
                            "F1",
                            "https://www.flashscore.com/football/france/ligue-1/"
                    ),

                    Map.entry(
                            "championship",
                            "https://www.flashscore.com/football/england/championship/"
                    ),

                    Map.entry(
                            "europa-league",
                            "https://www.flashscore.com/football/europe/europa-league/"
                    )


            );

    public static String getFixturesUrl(String league) {

        String base = BASE_URLS.get(league);

        if(base == null){

            throw new RuntimeException(
                    "Fant ikke league URL for: " + league
            );
        }

        return base + "fixtures/";
    }

    public static String getResultsUrl(String league) {

        String base = BASE_URLS.get(league);

        if(base == null){

            throw new RuntimeException(
                    "Fant ikke league URL for: " + league
            );
        }

        return base + "results/";
    }
    public static String getOddsUrl(String league) {
        return BASE_URLS.get(league);
    }

}