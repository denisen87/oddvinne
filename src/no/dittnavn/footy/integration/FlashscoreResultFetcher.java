package no.dittnavn.footy.integration;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import no.dittnavn.footy.config.LeagueConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FlashscoreResultFetcher {

    public static class MatchResult {
        public String homeTeam;
        public String awayTeam;
        public int homeGoals;
        public int awayGoals;
        public String date;
    }

    public static List<MatchResult> fetchResults(List<String> leagues) {

        List<MatchResult> results = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);

        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            for (String league : leagues) {

                String url = LeagueConfig.getResultsUrl(league);
                driver.get(url);

                try {
                    WebElement acceptBtn = wait.until(
                            ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[contains(.,'Accept')]")
                            )
                    );
                    acceptBtn.click();
                } catch (Exception ignored) {}

                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".event__match")
                ));

                List<WebElement> matches =
                        driver.findElements(By.cssSelector(".event__match"));

                for (WebElement match : matches) {

                    try {

                        String home = match.findElement(
                                By.cssSelector(".event__homeParticipant")
                        ).getText();

                        String away = match.findElement(
                                By.cssSelector(".event__awayParticipant")
                        ).getText();

                        String homeScore = match.findElement(
                                By.cssSelector(".event__score--home")
                        ).getText();

                        String awayScore = match.findElement(
                                By.cssSelector(".event__score--away")
                        ).getText();

                        int homeGoals = Integer.parseInt(homeScore);
                        int awayGoals = Integer.parseInt(awayScore);

                        String result;

                        if (homeGoals > awayGoals) result = "HOME";
                        else if (homeGoals < awayGoals) result = "AWAY";
                        else result = "DRAW";


                        MatchResult r = new MatchResult();
                        r.homeTeam = home;
                        r.awayTeam = away;
                        r.homeGoals = homeGoals;
                        r.awayGoals = awayGoals;
                        r.date = java.time.LocalDate.now().toString();

                        results.add(r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } finally {
            driver.quit();
        }

        System.out.println("Results found: " + results.size());
        return results;
    }

    public static List<MatchResult> fetchEuropaLeague2025Results() {
        return fetchResults(List.of("europa-league"));
    }
}