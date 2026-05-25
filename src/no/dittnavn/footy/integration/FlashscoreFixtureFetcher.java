package no.dittnavn.footy.integration;

import no.dittnavn.footy.config.LeagueConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlashscoreFixtureFetcher {

    public static class Fixture {

        public String matchId;

        public String homeTeam;
        public String awayTeam;

        public String oddsHome;
        public String oddsDraw;
        public String oddsAway;

        public String date;
    }

    public static List<Fixture> fetchFixtures(
            List<String> leagues
    ) {

        List<Fixture> fixtures =
                new ArrayList<>();

        ChromeOptions options =
                new ChromeOptions();

        options.addArguments("--start-maximized");

        WebDriver driver =
                new ChromeDriver(options);

        try {


            for (String league : leagues) {

                String url =
                        LeagueConfig.getOddsUrl(
                                league
                        );

                System.out.println(
                        "OPENING URL: " + url
                );

                driver.get(url);

                WebDriverWait wait =
                        new WebDriverWait(
                                driver,
                                Duration.ofSeconds(10)
                        );

                wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector(".event__match")
                        )
                );

                List<WebElement> rows =
                        driver.findElements(
                                By.cssSelector(
                                        ".event__header, .event__match, .wclLeagueHeader"
                                )
                        );

                System.out.println(
                        "ROWS FOUND: "
                                + rows.size()
                );

                Set<String> usedTeams =
                        new HashSet<>();

                int lastY = -1;

                for (WebElement row : rows) {

                    int currentY =
                            row.getLocation().getY();

                    if(lastY != -1){

                        int gap =
                                currentY - lastY;

                        System.out.println(
                                "GAP: " + gap
                        );

                        // stor vertikal avstand
                        // betyr ny dato/section

                        if(gap > 80){

                            System.out.println(
                                    "STOPPING - LARGE GAP DETECTED"
                            );

                            break;
                        }
                    }

                    lastY = currentY;

                    try {

                        String text =
                                row.getText();

                        String classes =
                                row.getAttribute("class");

                        System.out.println(
                                "ROW TEXT: " + text
                        );

                        // =====================================
                        // STOPP når Scheduled starter
                        // =====================================

                        if (classes.contains("wclLeagueHeader")) {

                            String headerText =
                                    row.getText();

                            System.out.println(
                                    "HEADER: "
                                            + headerText
                            );

                            if (headerText.contains("Scheduled")) {

                                System.out.println(
                                        "STOPPING AT SCHEDULED SECTION"
                                );

                                break;
                            }

                            continue;
                        }

                        // =====================================
                        // Kun ekte matcher
                        // =====================================

                        if (!classes.contains("event__match")) {

                            continue;
                        }

                        // =====================================
                        // Må ha tidspunkt
                        // =====================================

                        List<WebElement> timeEls =
                                row.findElements(
                                        By.cssSelector(".event__time")
                                );

                        if (timeEls.isEmpty()) {

                            continue;
                        }

                        String timeText =
                                timeEls.get(0).getText();

                        if (!timeText.contains(":")) {

                            continue;
                        }

                        // =====================================
                        // MATCH ID
                        // =====================================

                        String rawId =
                                row.getAttribute("id");

                        if (rawId == null) {

                            continue;
                        }

                        Fixture f =
                                new Fixture();

                        if (rawId.startsWith("g_1_")) {

                            f.matchId =
                                    rawId.replace(
                                            "g_1_",
                                            ""
                                    );
                        }

                        // =====================================
                        // TEAMS
                        // =====================================

                        f.homeTeam =
                                row.findElement(
                                        By.cssSelector(
                                                ".event__homeParticipant"
                                        )
                                ).getText();

                        f.awayTeam =
                                row.findElement(
                                        By.cssSelector(
                                                ".event__awayParticipant"
                                        )
                                ).getText();

                        String home =
                                f.homeTeam.toLowerCase();

                        String away =
                                f.awayTeam.toLowerCase();

                        // =====================================
                        // DUPLIKAT FILTER
                        // =====================================

                        if (usedTeams.contains(home)
                                || usedTeams.contains(away)) {

                            System.out.println(
                                    "SKIPPING DUPLICATE: "
                                            + f.homeTeam
                                            + " vs "
                                            + f.awayTeam
                            );

                            continue;
                        }

                        usedTeams.add(home);
                        usedTeams.add(away);

                        // =====================================
                        // DATO
                        // =====================================

                        f.date =
                                LocalDate.now()
                                        .toString();

                        System.out.println(
                                "FOUND MATCH: "
                                        + f.homeTeam
                                        + " vs "
                                        + f.awayTeam
                        );

                        fixtures.add(f);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            driver.quit();
        }

        return fixtures;
    }
}