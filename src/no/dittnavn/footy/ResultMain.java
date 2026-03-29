package no.dittnavn.footy;

import no.dittnavn.footy.db.DatabaseManager;
import service.ResultUpdater;

public class ResultMain {

    public static void main(String[] args) {

        DatabaseManager.init();   // oppretter tabeller hvis mangler
        ResultUpdater.updateResults();

        System.out.println("=== RESULT UPDATE FERDIG ===");
    }
}