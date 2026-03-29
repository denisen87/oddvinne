package no.dittnavn.footy.config;

import java.util.Map;

public class LeagueConfig {

    private static final Map<String, String> BASE_URLS = Map.of(
            "championship", "https://www.flashscore.com/football/england/championship/",
            "europa-league", "https://www.flashscore.com/football/europe/europa-league/"
    );

    public static String getFixturesUrl(String league) {
        return BASE_URLS.get(league) + "fixtures/";
    }

    public static String getResultsUrl(String league) {
        return BASE_URLS.get(league) + "results/";
    }
}