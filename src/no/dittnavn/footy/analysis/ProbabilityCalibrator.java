package no.dittnavn.footy.analysis;

public class ProbabilityCalibrator {

    // hvor mye vi drar mot midten
    private double strength = 0.35;

    public double calibrate(double raw){

        // trekk ekstreme verdier mot 0.5
        double adjusted =
                raw * (1 - strength) +
                        0.5 * strength;

        // clamp
        if(adjusted < 0.02) adjusted = 0.02;
        if(adjusted > 0.98) adjusted = 0.98;

        return adjusted;
    }
}
