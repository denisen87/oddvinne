package no.dittnavn.footy.analysis;

import no.dittnavn.footy.analysis.learning.AutoDataTrainer;
import no.dittnavn.footy.integration.FootballDataClient;
import no.dittnavn.footy.integration.FootballDataParser;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.stats.StatsIndeks;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AutoUpdater {

    private final StatsIndeks indeks;
    private final AutoDataTrainer trainer;

    public AutoUpdater(StatsIndeks indeks, AutoDataTrainer trainer) {
        this.indeks = indeks;
        this.trainer = trainer;
    }

    public void run() {
        System.out.println("\n=== AUTO UPDATE AV ALLE LIGAER ===");

        updateLeague("PL");   // England
        updateLeague("PD");   // Spania
        updateLeague("BL1");  // Tyskland
        updateLeague("SA");   // Italia
        updateLeague("FL1");  // Frankrike
        updateLeague("DED");  // Nederland
        updateLeague("PPL");  // Portugal
        updateLeague("ELC");  // Championship

        System.out.println("=== AUTO UPDATE FULLFØRT ===\n");
    }

    private void updateLeague(String code) {
        try {

            String lastDate = indeks.getLastUpdateDate(code);

            String json;

            if (lastDate == null) {
                System.out.println("Første gang: henter full historikk for " + code);
                json = FootballDataClient.fetchMatchesFromCompetition(code);
            } else {
                System.out.println("Oppdaterer " + code + " fra dato: " + lastDate);
                json = FootballDataClient.fetchMatchesFromCompetitionFromDate(code, lastDate);
            }

            if (json == null || json.contains("errorCode")) {
                System.out.println("⚠️ Ingen data fra API (rate limit / feil).");
                return;
            }

            List<Match> matches = FootballDataParser.parseMatches(json);

            if (!matches.isEmpty()) {
                trainer.trainFromMatches(matches);
                indeks.setLastUpdateDate(code, LocalDate.now().toString());
            }

            Thread.sleep(1200);

        } catch (IOException | InterruptedException e) {
            System.out.println("API error: " + e.getMessage());
        }
    }
}