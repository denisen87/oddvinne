package no.dittnavn.footy.loader;

import no.dittnavn.footy.model.Match;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvHistoricalLoader {

    public static List<Match> load(String path, String league) {

        List<Match> matches = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String header = br.readLine();
            if (header == null) return matches;

            boolean isStandardFormat = !league.equalsIgnoreCase("eliteserien");

            String[] headers = header.split("[,;\t]");

            // 🔥 find odds columns (only used in standard format)
            int homeOddsIdx = findIndex(headers, "B365H", "PSH", "AvgH");
            int drawOddsIdx = findIndex(headers, "B365D", "PSD", "AvgD");
            int awayOddsIdx = findIndex(headers, "B365A", "PSA", "AvgA");

            int hsIdx = findIndex(headers, "HS");
            int asIdx = findIndex(headers, "AS");
            int hstIdx = findIndex(headers, "HST");
            int astIdx = findIndex(headers, "AST");
            int hcIdx = findIndex(headers, "HC");
            int acIdx = findIndex(headers, "AC");


            int hfIdx = findIndex(headers, "HF");
            int afIdx = findIndex(headers, "AF");
            int hyIdx = findIndex(headers, "HY");
            int ayIdx = findIndex(headers, "AY");

            String line;
            int errors = 0;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                line = line.replace("\"", "");

                String delimiter;

                if (header.contains(";")) {
                    delimiter = ";";
                } else if (header.contains("\t")) {
                    delimiter = "\t";
                } else {
                    delimiter = ",";
                }

                String[] parts = line.split(delimiter, -1);

                try {

                    String date;
                    String homeTeam;
                    String awayTeam;
                    int homeGoals;
                    int awayGoals;

                    double homeOdds = -1;
                    double drawOdds = -1;
                    double awayOdds = -1;

                    if (isStandardFormat) {
                        // ✅ STANDARD EUROPE FORMAT

                        if (parts.length < 7) continue;

                        date = safeDate(parts);
                        homeTeam = parts[3].trim();
                        awayTeam = parts[4].trim();

                        homeGoals = safeInt(parts[5]);
                        awayGoals = safeInt(parts[6]);

                        System.out.println("HEADERS CHECK:");
                        System.out.println("PSCH idx: " + findIndex(headers, "PSCH"));
                        System.out.println("MaxCH idx: " + findIndex(headers, "MaxCH"));
                        System.out.println("AvgCH idx: " + findIndex(headers, "AvgCH"));

                        if (date.equals("UNKNOWN")) {
                            System.out.println("⚠️ Could not parse date: " + String.join(",", parts));
                        }

                        int psch = findIndex(headers, "PSCH");
                        int maxch = findIndex(headers, "MaxCH");
                        int avgch = findIndex(headers, "AvgCH");

                        System.out.println("VALUES RAW:");
                        if (psch != -1) System.out.println("PSCH: " + parts[psch]);
                        if (maxch != -1) System.out.println("MaxCH: " + parts[maxch]);
                        if (avgch != -1) System.out.println("AvgCH: " + parts[avgch]);

                        double p = parse(parts, psch);
                        double m = parse(parts, maxch);
                        double a = parse(parts, avgch);

                        System.out.println("PARSED -> PSCH=" + p + " MaxCH=" + m + " AvgCH=" + a);

                        homeOdds = firstValid(p, m, a);

                        homeOdds = firstValid(
                                parse(parts, findIndex(headers, "PSCH")),
                                parse(parts, findIndex(headers, "MaxCH")),
                                parse(parts, findIndex(headers, "AvgCH"))
                        );

                        drawOdds = firstValid(
                                parse(parts, findIndex(headers, "PSCD")),
                                parse(parts, findIndex(headers, "MaxCD")),
                                parse(parts, findIndex(headers, "AvgCD"))
                        );

                        awayOdds = firstValid(
                                parse(parts, findIndex(headers, "PSCA")),
                                parse(parts, findIndex(headers, "MaxCA")),
                                parse(parts, findIndex(headers, "AvgCA"))
                        );

                        System.out.println(
                                "LOADER ODDS -> " +
                                        homeTeam + " vs " + awayTeam +
                                        " | " + homeOdds + "/" + drawOdds + "/" + awayOdds
                        );

                    } else {
                        // 🔥 NORWAY / CUSTOM FORMAT

                        if (parts.length < 9) continue;

                        date = parts[3].replace(".", "/");
                        homeTeam = parts[5].trim();
                        awayTeam = parts[6].trim();

                        homeGoals = safeInt(parts[7]);
                        awayGoals = safeInt(parts[8]);

                        // 🔥 find the first valid odds triple
                        for (int i = 0; i < parts.length - 2; i++) {


                            double h = parseOdds(parts[i]);
                            double d = parseOdds(parts[i + 1]);
                            double a = parseOdds(parts[i + 2]);

                            if (h > 1.01 && d > 1.01 && a > 1.01) {
                                homeOdds = h;
                                drawOdds = d;
                                awayOdds = a;
                                break;
                            }
                        }

                        if (date.equals("UNKNOWN")) {
                            System.out.println("⚠️ UNKNOWN DATE ROW: " + String.join(",", parts));
                        }

                    }

                    // 🔥 skip if no valid odds found
                    if (homeOdds <= 1.01 || drawOdds <= 1.01 || awayOdds <= 1.01) {
                        continue;
                    }

                    Match match = new Match(
                            date,
                            homeTeam,
                            awayTeam,
                            homeGoals,
                            awayGoals
                    );


                    match.setHomeShots(parseIntSafe(parts, hsIdx));
                    match.setAwayShots(parseIntSafe(parts, asIdx));

                    match.setHomeShotsTarget(parseIntSafe(parts, hstIdx));
                    match.setAwayShotsTarget(parseIntSafe(parts, astIdx));

                    match.setHomeCorners(parseIntSafe(parts, hcIdx));
                    match.setAwayCorners(parseIntSafe(parts, acIdx));

                    match.setHomeFouls(parseIntSafe(parts, hfIdx));
                    match.setAwayFouls(parseIntSafe(parts, afIdx));

                    match.setHomeYellow(parseIntSafe(parts, hyIdx));
                    match.setAwayYellow(parseIntSafe(parts, ayIdx));

                    if (hsIdx == -1) {
                        System.out.println("⚠️ HS not found in " + league);
                    }

                    match.setLeague(league);
                    match.setHomeOdds(homeOdds);
                    match.setDrawOdds(drawOdds);
                    match.setAwayOdds(awayOdds);

                    matches.add(match);

                } catch (Exception e) {
                    errors++;
                    System.out.println("Row parse error: " + e.getMessage() + " | Line: " + line);
                }
            }

            System.out.println("LOADED matches with odds: " + matches.size() + " | Errors: " + errors);

        } catch (Exception e) {
            System.out.println("CSV HISTORICAL load error: " + e.getMessage());
            e.printStackTrace(); // 🔥 remove this later if you don't need the stack trace
        }


        return matches;
    }

    // 🔍 find column index
    private static int findIndex(String[] headers, String... names) {
        for (String name : names) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase(name)){
                    return i;
                }
            }
        }
        return -1;
    }

    // 🔢 parse from index
    private static double parse(String[] parts, int idx) {
        if (idx < 0 || idx >= parts.length) return -1;

        try {
            String val = parts[idx].trim().replace(",", ".");
            if (val.isEmpty()) return -1;
            return Double.parseDouble(val);
        } catch (Exception e) {
            return -1;
        }
    }

    // 🔢 parse odds string
    private static double parseOdds(String s) {
        try {
            if (s == null) return -1;

            s = s.trim().replace(",", ".");

            if (s.isEmpty()) return -1;

            return Double.parseDouble(s);

        } catch (Exception e) {
            return -1;
        }

    }



    // 🔢 safe int
    private static int safeInt(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return 0;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static double firstValid(double... values) {
        for (double v : values) {
            if (v > 1.01) return v;
        }
        return -1;
    }

    private static int parseIntSafe(String[] parts, int idx) {
        if (idx < 0 || idx >= parts.length) return -1;

        try {
            String val = parts[idx].trim();
            if (val.isEmpty()) return -1;
            return Integer.parseInt(val);
        } catch (Exception e) {
            return -1;
        }
    }

    private static String safeDate(String[] parts) {
        for (String p : parts) {

            p = p.trim();

            // dd/mm/yyyy
            if (p.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}")) return p;

            // yyyy-mm-dd
            if (p.matches("\\d{4}-\\d{2}-\\d{2}")) return p;


            // 🔥 NY: dd.mm.yyyy
            if (p.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                return p.replace(".", "/");


            }

        }
        return "UNKNOWN";
    }


}