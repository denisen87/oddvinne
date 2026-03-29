package no.dittnavn.footy.tools;

import java.io.*;
import java.util.*;

public class OddsTextConverter {

    public static void main(String[] args) throws Exception {

        List<String> lines = readLines("data/raw_odds.txt");
        List<String[]> matches = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            // Finn start på kamp = lagnavn
            if (isTeam(line)) {

                String home = line;

                int j = i + 1;

                // hopp duplikat av hjemmelag
                while (j < lines.size() && lines.get(j).trim().equals(home)) {
                    j++;
                }

                // hopp separatorer / klokkeslett / dato / tomme linjer
                while (j < lines.size() && !isTeam(lines.get(j).trim())) {
                    j++;
                }

                if (j >= lines.size()) continue;

                String away = lines.get(j).trim();

                int k = j + 1;

                // hopp duplikat bortelag
                while (k < lines.size() && lines.get(k).trim().equals(away)) {
                    k++;
                }

                // hopp til odds
                while (k < lines.size() && !isOdd(lines.get(k).trim())) {
                    k++;
                }

                if (k + 2 >= lines.size()) continue;

                String homeOdds = lines.get(k).trim();
                String drawOdds = lines.get(k + 1).trim();
                String awayOdds = lines.get(k + 2).trim();

                matches.add(new String[]{
                        home,
                        away,
                        homeOdds,
                        drawOdds,
                        awayOdds
                });

                i = k + 2;
            }
        }

        writeCsv(matches);

        System.out.println("✅ odds_today.csv generert automatisk (OddsPortal klar)");
    }

    // -------------------------
    // HJELPEMETODER
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

    private static void writeCsv(List<String[]> matches) throws Exception {
        PrintWriter pw = new PrintWriter("data/odds_today.csv");
        pw.println("HomeTeam,AwayTeam,HomeOdds,DrawOdds,AwayOdds");

        for (String[] m : matches) {
            pw.println(String.join(",", m));
        }

        pw.close();
    }

    private static boolean isOdd(String s) {
        return s.matches("\\d+\\.\\d+");
    }

    private static boolean isTeam(String s) {

        if (s.length() < 3) return false;

        // ikke klokkeslett
        if (s.matches("\\d{2}:\\d{2}")) return false;

        // ikke dato
        if (s.matches("\\d{1,2}.*\\d{4}")) return false;

        // ikke odds
        if (isOdd(s)) return false;

        // ikke separator
        if (s.equals("-") || s.equals("–")) return false;

        // ikke oddsportal header
        if (s.equalsIgnoreCase("1") ||
                s.equalsIgnoreCase("X") ||
                s.equalsIgnoreCase("2") ||
                s.equalsIgnoreCase("B's")) return false;

        return true;
    }
}