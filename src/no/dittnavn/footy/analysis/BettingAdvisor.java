package no.dittnavn.footy.analysis;

public class BettingAdvisor {

    public void giveAdvice(double valueHome, double valueDraw, double valueAway) {

        System.out.println("\n=== BETTING RÅD ===");

        if (valueHome > 0.15) {
            System.out.println("🔥 STERKT SPILL: Hjemmeseier");
        }
        else if (valueAway > 0.15) {
            System.out.println("🔥 STERKT SPILL: Borteseier");
        }
        else if (valueDraw > 0.15) {
            System.out.println("🔥 STERKT SPILL: Uavgjort");
        }

        else if (valueHome > 0.05) {
            System.out.println("👍 OK spill: Hjemmeseier");
        }
        else if (valueAway > 0.05) {
            System.out.println("👍 OK spill: Borteseier");
        }
        else if (valueDraw > 0.05) {
            System.out.println("👍 OK spill: Uavgjort");
        }

        else {
            System.out.println("❌ INGEN VALUE — ikke spill denne kampen");
        }
    }
}
