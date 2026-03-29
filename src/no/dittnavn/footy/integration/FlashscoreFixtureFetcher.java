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

public class FlashscoreFixtureFetcher {

    public static class Fixture {
        public String homeTeam;
        public String awayTeam;
        public String date;
    }

    public static List<Fixture> fetchFixtures(List<String> leagues) {

        List<Fixture> fixtures = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);

        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            for (String league : leagues) {

                String url = LeagueConfig.getFixturesUrl(league);
                driver.get(url);


                try {
                    WebElement acceptBtn = wait.until(
                            ExpectedConditions.elementToBeClickable(
                                    By.xpath("//button[contains(.,'Accept') or contains(.,'Agree')]")
                            )
                    );
                    acceptBtn.click();
                } catch (Exception ignored) {}

                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[class*='event__match']")
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

                        Fixture f = new Fixture();
                        f.homeTeam = home;
                        f.awayTeam = away;
                        f.date = java.time.LocalDate.now().toString();

                        fixtures.add(f);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } finally {
            driver.quit();
        }

        System.out.println("Fixtures found: " + fixtures.size());
        return fixtures;
    }
}