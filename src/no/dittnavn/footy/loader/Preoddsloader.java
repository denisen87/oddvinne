package no.dittnavn.footy.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Preoddsloader {

    public static List<OddsRow> load(String path) {
        List<OddsRow> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) return list;

            // 🔥 auto-detect delimiter
            String delimiter = header.contains(";") ? ";" : ",";
            System.out.println("Using delimiter: " + delimiter);

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(delimiter, -1);

                // 🔥 skip broken rows
                if (parts.length < 11) {
                    System.out.println("SKIP (too short): " + line);
                    continue;
                }

                try {
                    String home = parts[5].trim();
                    String away = parts[6].trim();
                    String market = parts[7].trim();
                    String label = parts[8].trim();

                    // 🔥 håndter ødelagt price (2;39 case)
                    String priceRaw = parts[10].trim();

                    if (priceRaw.isEmpty() && parts.length > 11) {
                        priceRaw = parts[10] + "." + parts[11];
                    }

                    // 🔥 clean price
                    priceRaw = priceRaw
                            .replace("\"", "")
                            .replace(",", ".")
                            .trim();

                    if (priceRaw.isEmpty()) continue;

                    double price = Double.parseDouble(priceRaw);

                    // 🔥 filter kun h2h
                    if (!market.toLowerCase().contains("h2h")) continue;

                    list.add(new OddsRow(home, away, label, price));

                } catch (Exception e) {
                    System.out.println("SKIP (parse error): " + line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("TOTAL ROWS: " + list.size());
        return list;
    }
}