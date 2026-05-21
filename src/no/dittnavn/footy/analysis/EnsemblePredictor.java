package no.dittnavn.footy.analysis;

import no.dittnavn.footy.stats.TeamStats;

import no.dittnavn.footy.analysis.learning.ModelWeights;

import no.dittnavn.footy.analysis.features.MatchFeatures;

import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.tools.OddsTextConverter;
import no.dittnavn.footy.tools.OddsConverter;


import no.dittnavn.footy.tools.OddsTextConverter;

public class EnsemblePredictor {

    public static double[] calculateFinalProbs(
            TeamStats home,
            TeamStats away,
            double oddsHome,
            double oddsDraw,
            double oddsAway,
            ModelWeights weights,
            NeuralModel neural) {

        MatchFeatures features = new MatchFeatures(home, away, 1.0, 1.0, 1.0);
        double[] neuralProbs = neural.predictAll(features);

        // 🔥 NEURAL STABILIZER
        for (int i = 0; i < 3; i++) {
            if (neuralProbs[i] < 0.05) neuralProbs[i] = 0.05;
            if (neuralProbs[i] > 0.90) neuralProbs[i] = 0.90;
        }

// re-normaliser
        double sumNeural = neuralProbs[0] + neuralProbs[1] + neuralProbs[2];
        for (int i = 0; i < 3; i++) {
            neuralProbs[i] /= sumNeural;
        }

        ProbabilityAnalysis prob = new ProbabilityAnalysis(weights);
        EloPredictor elo = new EloPredictor();
        OddsConverter oddsConv = new OddsConverter();

        double pHome = prob.homeWinProbability(home, away);
        double pDraw = prob.drawProbability(home, away);
        double pAway = prob.awayWinProbability(home, away);

        double[] eloProbs = elo.predict(home, away);
        double[] oddsProbs = oddsConv.fromOdds(oddsHome, oddsDraw, oddsAway);

        double finalHome =
                0.45 * oddsProbs[0] +
                        0.20 * pHome +
                        0.20 * eloProbs[0] +
                        0.15 * neuralProbs[0];

        double finalDraw =
                0.45 * oddsProbs[1] +
                        0.20 * pDraw +
                        0.20 * eloProbs[1] +
                        0.15 * neuralProbs[1];

        double finalAway =
                0.45 * oddsProbs[2] +
                        0.20 * pAway +
                        0.20 * eloProbs[2] +
                        0.15 * neuralProbs[2];

        double sum = finalHome + finalDraw + finalAway;

        return new double[]{
                finalHome / sum,
                finalDraw / sum,
                finalAway / sum
        };
    }

    public void updatePerformance(
            double[] neural,
            double[] elo,
            double[] poisson,
            double[] odds,
            double actualHome,
            double actualDraw,
            double actualAway) {

        double[] actual = {actualHome, actualDraw, actualAway};

        double errorNeural = error(neural, actual);
        double errorElo = error(elo, actual);
        double errorPoisson = error(poisson, actual);
        double errorOdds = error(odds, actual);

        System.out.println("MODEL ERROR:");
        System.out.println("Neural: " + errorNeural);
        System.out.println("ELO: " + errorElo);
        System.out.println("Poisson: " + errorPoisson);
        System.out.println("Odds: " + errorOdds);
    }

    private double error(double[] pred, double[] actual) {
        double e = 0;
        for (int i = 0; i < 3; i++) {
            e += Math.abs(pred[i] - actual[i]);
        }
        return e;
    }
}