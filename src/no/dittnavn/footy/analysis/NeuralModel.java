package no.dittnavn.footy.analysis;

import no.dittnavn.footy.analysis.features.MatchFeatures;

public class NeuralModel {

    // feature weights
    private double wRating = 0.4;
    private double wForm = 0.3;
    private double wWinrate = 0.2;
    private double wMotivation = 0.1;
    private double wFatigue = -0.1;
    private double wCoach = 0.1;
    private double wInjury = -0.2;

    // market-learning weights
    private double wMarket = 0.2;
    private double wProfit = 0.3;

    private double wAttack = 0.2;
    private double wDefense = 0.2;
    private double wTempo = 0.1;
    private double wComeback = 0.1;
    private double wChoke = -0.1;

    // Learning rate
    private double learningRate = 0.005;

    // Feature scaling parameters
    private static final double MAX_RATING_DIFF = 500; // Maksimal forskjell i rating (kan justeres)
    private static final double MAX_FORM_DIFF = 10;   // Maksimal forskjell i form (kan justeres)
    private static final double MAX_WINRATE_DIFF = 1;  // Maksimal forskjell i winrate (0 - 1)

    // Softmax funksjon for prediksjon
    private double[] softmax(double[] scores) {
        double sum = 0.0;
        for (double score : scores) {
            sum += Math.exp(score);
        }

        double[] probabilities = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            probabilities[i] = Math.exp(scores[i]) / sum;
        }

        return probabilities;
    }

    // Prediksjon for kampens utfall
    public double[] predictAll(MatchFeatures f) {

        double ratingDiffScaled = f.ratingDiff / MAX_RATING_DIFF;
        double formDiffScaled = f.formDiff / MAX_FORM_DIFF;
        double winrateDiffScaled = f.winrateDiff / MAX_WINRATE_DIFF;

        double base =
                wRating * ratingDiffScaled +
                        wForm * formDiffScaled +
                        wWinrate * winrateDiffScaled +
                        wAttack * f.attackDiff +
                        wDefense * f.defenseDiff +
                        wTempo * f.tempoDiff +
                        wCoach * f.coachDiff +
                        wFatigue * f.fatigueDiff +
                        wChoke * f.chokeDiff;

        double homeScore = base;
        double awayScore = -base;
        double drawScore = -Math.abs(base) * 0.5;

        return softmax(new double[]{homeScore, drawScore, awayScore});
    }

    // Trening av modellen med faktiske resultater
    public void train(MatchFeatures f, double actual, double profit, double combined) {
        // Prediksjon
        double[] predicted = predictAll(f);

        // Beregn feilen
        double errorHome = actual == 1 ? 1 - predicted[0] : 0 - predicted[0];
        double errorDraw = actual == 0 ? 1 - predicted[1] : 0 - predicted[1];
        double errorAway = actual == -1 ? 1 - predicted[2] : 0 - predicted[2];

        // Oppdater vektene for hver feature
        wRating += learningRate * (errorHome * f.ratingDiff + errorDraw * f.ratingDiff + errorAway * f.ratingDiff);
        wForm += learningRate * (errorHome * f.formDiff + errorDraw * f.formDiff + errorAway * f.formDiff);
        wWinrate += learningRate * (errorHome * f.winrateDiff + errorDraw * f.winrateDiff + errorAway * f.winrateDiff);
        wAttack += learningRate * (errorHome * f.attackDiff + errorDraw * f.attackDiff + errorAway * f.attackDiff);
        wDefense += learningRate * (errorHome * f.defenseDiff + errorDraw * f.defenseDiff + errorAway * f.defenseDiff);
        wTempo += learningRate * (errorHome * f.tempoDiff + errorDraw * f.tempoDiff + errorAway * f.tempoDiff);
        wCoach += learningRate * (errorHome * f.coachDiff + errorDraw * f.coachDiff + errorAway * f.coachDiff);
        wFatigue += learningRate * (errorHome * f.fatigueDiff + errorDraw * f.fatigueDiff + errorAway * f.fatigueDiff);
        wChoke += learningRate * (errorHome * f.chokeDiff + errorDraw * f.chokeDiff + errorAway * f.chokeDiff);

        // Market IQ learning
        wMarket += learningRate * combined;
        wProfit += learningRate * profit;
    }

    // Vurder om det er verdt å satse
    public boolean shouldBet(double probability, double odds) {
        double implied = 1.0 / odds;
        double edge = probability - implied;

        return edge > 0.03; // Edge-kriterie
    }

    // Skriver ut modellens vekter for debugging
    public void printWeights() {
        System.out.println("\n=== AI WEIGHTS ===");
        System.out.printf("Rating: %.3f\n", wRating);
        System.out.printf("Form: %.3f\n", wForm);
        System.out.printf("Winrate: %.3f\n", wWinrate);
        System.out.printf("Motivation: %.3f\n", wMotivation);
        System.out.printf("Fatigue: %.3f\n", wFatigue);
        System.out.printf("Coach: %.3f\n", wCoach);
        System.out.printf("Injury: %.3f\n", wInjury);
        System.out.printf("Market IQ: %.3f\n", wMarket);
        System.out.printf("Profit IQ: %.3f\n", wProfit);
    }
}
