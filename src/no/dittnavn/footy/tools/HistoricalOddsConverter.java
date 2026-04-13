package no.dittnavn.footy.tools;

import java.io.*;
import java.util.*;

public class HistoricalOddsConverter {

    public static void main(String[] args) throws Exception {

        // 🔥 Endre denne til den TXT-filen du vil konvertere
        File inputFile = new File("data/historical_championsleague.txt");

        if (!inputFile.exists()) {
            System.out.println("Fant ikke fil: " + inputFile.getPath());
            return;
        }

        // 🔥 Hent liga automatisk fra filnavnet
        String fileName = inputFile.getName();

        String league = fileName
                .replace("historical_", "")
                .replace(".txt", "")
                .split("_")[0];

        league = mapLeague(league);


        System.out.println("Konverterer liga: " + league);

        List<String> lines = readLines(inputFile.getPath());

        System.out.println("Totale linjer: " + lines.size());

        for (int j = 0; j < 40 && j < lines.size(); j++) {
            System.out.println(j + ": [" + lines.get(j) + "]");
        }
        List<String[]> matches = new ArrayList<>();



        String currentDate = null;

        for (int i = 9; i < lines.size(); i++){

            String line = lines.get(i).trim();

            if (line.matches("\\d{1,2} [A-Za-z]{3} \\d{4}.*")) {
                currentDate = convertDate(line.substring(0, 11));
                System.out.println("Fant dato: " + currentDate);
                continue;
            }

            // 🔥 Er dette awayOdds?
            if (!line.matches("\\d+\\.\\d+")) continue;

            String drawOdds = lines.get(i - 1).trim();
            String homeOdds = lines.get(i - 2).trim();

            if (!drawOdds.matches("\\d+\\.\\d+")) continue;
            if (!homeOdds.matches("\\d+\\.\\d+")) continue;

            String away = lines.get(i - 4).trim();
            String awayGoals = lines.get(i - 5).trim();
            String dash = lines.get(i - 6).trim();
            String homeGoals = lines.get(i - 7).trim();
            String home = lines.get(i - 9).trim();

            if (!awayGoals.matches("\\d+")) continue;
            if (!homeGoals.matches("\\d+")) continue;
            if (!dash.equals("–")) continue;

            // 🔥 Sikkerhet: legg ALDRI til uten dato
            if (currentDate == null) {
                System.out.println("Hopper over kamp uten dato: " + home + " vs " + away);
                continue;
            }

            matches.add(new String[]{
                    currentDate,
                    home,
                    away,
                    homeGoals,
                    awayGoals,
                    homeOdds,
                    drawOdds,
                    line
            });

            System.out.println("Fant kamp: " + home + " vs " + away + " | " + currentDate);
        }

        System.out.println("Totalt funnet: " + matches.size());
        writeCsv(matches, league);

    }

    // -------------------------

    private static List<String> readLines(String path) throws Exception {
        List<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String l;
        while ((l = br.readLine()) != null) {
            if (!l.trim().isEmpty())
                list.add(l.trim());
        }
        br.close();
        return list;
    }


    private static void writeCsv(List<String[]> matches, String league) throws Exception {

        File outFile = new File("data/historical_" + league + ".csv");

        System.out.println("Skriver til: " + outFile.getAbsolutePath());

        PrintWriter pw = new PrintWriter(outFile);

        pw.println("Date,HomeTeam,AwayTeam,FTHG,FTAG,HomeOdds,DrawOdds,AwayOdds");

        for (String[] m : matches) {
            pw.println(String.join(",", m));
        }

        pw.close();

        System.out.println("CSV skrevet ferdig!");
    }

    private static boolean isNoise(String s) {

        if (s.equalsIgnoreCase("1") ||
                s.equalsIgnoreCase("X") ||
                s.equalsIgnoreCase("2") ||
                s.equalsIgnoreCase("B's"))
            return true;

        if (s.matches("\\d{2}:\\d{2}")) return true;

        return false;
    }

    private static boolean isTeam(String s) {
        if (s.length() < 3) return false;
        if (s.matches("\\d+")) return false;
        if (s.equals("–")) return false;
        return true;
    }

    private static String convertDate(String rawDate) {

        Map<String, String> months = Map.ofEntries(
                Map.entry("Jan", "01"),
                Map.entry("Feb", "02"),
                Map.entry("Mar", "03"),
                Map.entry("Apr", "04"),
                Map.entry("May", "05"),
                Map.entry("Jun", "06"),
                Map.entry("Jul", "07"),
                Map.entry("Aug", "08"),
                Map.entry("Sep", "09"),
                Map.entry("Oct", "10"),
                Map.entry("Nov", "11"),
                Map.entry("Dec", "12")
        );

        String[] parts = rawDate.split(" ");
        String day = parts[0];
        String month = months.get(parts[1]);
        String year = parts[2];

        return year + "-" + month + "-" + String.format("%02d", Integer.parseInt(day));
    }

    private static String mapLeague(String league) {

        return switch (league.toLowerCase()) {
            case "championsleague" -> "CL";
            case "premierleague" -> "E0";
            case "laliga" -> "SP1";
            case "bundesliga" -> "D1";
            case "seriea" -> "I1";
            case "ligue1" -> "F1";
            default -> league.toUpperCase(); // fallback
        };
    }
}