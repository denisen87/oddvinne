package no.dittnavn.footy.analysis.ensemble;

import no.dittnavn.footy.analysis.ModelPerformance;
import no.dittnavn.footy.analysis.NeuralTrainer;

public class EnsemblePredictor {

    // 🔹 Dynamiske vekter (starter balansert)
    private double wNeural = 0.25;
    private double wElo = 0.25;
    private double wPoisson = 0.25;
    private double wOdds = 0.25;

    // 🔹 Performance tracking
    private ModelPerformance neuralPerf = new ModelPerformance();
    private ModelPerformance eloPerf = new ModelPerformance();
    private ModelPerformance poissonPerf = new ModelPerformance();
    private ModelPerformance oddsPerf = new ModelPerformance();

    private int totalUpdates = 0;

    // =====================================================
    // 🔥 COMBINE (bruker adaptive vekter)
    // =====================================================
    public double[] combine(
            double[] neural,
            double[] elo,
            double[] poisson,
            double[] odds) {

        // hent dynamisk neural-trust
        double neuralTrust = NeuralTrainer.getTrust();

        double adjustedNeural = wNeural * neuralTrust;

        double total = adjustedNeural + wElo + wPoisson + wOdds;

        double n = adjustedNeural / total;
        double e = wElo / total;
        double p = wPoisson / total;
        double o = wOdds / total;

        double home =
                neural[0] * n +
                        elo[0] * e +
                        poisson[0] * p +
                        odds[0] * o;

        double draw =
                neural[1] * n +
                        elo[1] * e +
                        poisson[1] * p +
                        odds[1] * o;

        double away =
                neural[2] * n +
                        elo[2] * e +
                        poisson[2] * p +
                        odds[2] * o;

        double sum = home + draw + away;

        home /= sum;
        draw /= sum;
        away /= sum;

        return new double[]{home, draw, away};
    }

    // =====================================================
    // 🔥 OPPDATER PERFORMANCE (kalles etter kamp)
    // =====================================================
    public void updatePerformance(
            double[] neural,
            double[] elo,
            double[] poisson,
            double[] odds,
            double actualHome,
            double actualDraw,
            double actualAway) {

        // 3-veis Brier
        neuralPerf.add(neural[0], actualHome);
        neuralPerf.add(neural[1], actualDraw);
        neuralPerf.add(neural[2], actualAway);

        eloPerf.add(elo[0], actualHome);
        eloPerf.add(elo[1], actualDraw);
        eloPerf.add(elo[2], actualAway);

        poissonPerf.add(poisson[0], actualHome);
        poissonPerf.add(poisson[1], actualDraw);
        poissonPerf.add(poisson[2], actualAway);

        oddsPerf.add(odds[0], actualHome);
        oddsPerf.add(odds[1], actualDraw);
        oddsPerf.add(odds[2], actualAway);

        totalUpdates++;

        // juster vekter hver 100 kamp
        if (totalUpdates % 100 == 0) {
            adaptWeights();
        }
    }

    // =====================================================
    // 🔥 ADAPTIVE VEKTJUSTERING
    // =====================================================
    private void adaptWeights() {

        // minst 100 datapunkter før justering
        if (neuralPerf.getCount() < 100) return;

        double sNeural = 1.0 / neuralPerf.getScore();
        double sElo = 1.0 / eloPerf.getScore();
        double sPoisson = 1.0 / poissonPerf.getScore();
        double sOdds = 1.0 / oddsPerf.getScore();

        double total = sNeural + sElo + sPoisson + sOdds;

        wNeural = sNeural / total;
        wElo = sElo / total;
        wPoisson = sPoisson / total;
        wOdds = sOdds / total;

        System.out.println("\n🔁 Ensemble weights oppdatert:");
        System.out.printf("Neural: %.3f | ELO: %.3f | Poisson: %.3f | Odds: %.3f\n",
                wNeural, wElo, wPoisson, wOdds);
    }
}