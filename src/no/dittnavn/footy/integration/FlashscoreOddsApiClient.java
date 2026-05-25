package no.dittnavn.footy.integration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class FlashscoreOddsApiClient {

    public static String[] getOdds(String matchId) {

        try {


            String urlStr =
                    "https://d.flashscore.com/x/feed/odds_1x2/" + matchId;

            URL url = new URL(urlStr);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            // 🔥 KRITISKE HEADERS
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("X-Fsign", "SW9D1eZo");
            conn.setRequestProperty("Referer",
                    "https://www.flashscore.com/");

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();

            String response = content.toString();

            // DEBUG
            if(response.length() < 50){
                System.out.println("⚠️ API response tom/for kort");
                return new String[]{"","",""};
            }

            // Flashscore feed parsing
            String[] parts = response.split("\\|");

            String home = "";
            String draw = "";
            String away = "";

            int found = 0;

            for(String p : parts){

                if(p.matches("\\d+\\.\\d+")){

                    if(found == 0) home = p;
                    else if(found == 1) draw = p;
                    else if(found == 2){
                        away = p;
                        break;
                    }

                    found++;
                }
            }

            return new String[]{home, draw, away};

        } catch (Exception e) {
            System.out.println("⚠️ Odds API feil: " + e.getMessage());
            return new String[]{"","",""};
        }
    }

    public static String[] getOddsHtml(String matchId) {

        ChromeOptions options = new ChromeOptions();

        WebDriver driver =
                new ChromeDriver(options);

        try {

            String url =
                    "https://www.flashscore.com/match/"
                            + matchId;

            System.out.println(
                    "OPEN URL: " + url
            );

            System.out.println(
                    "ENTER getOddsHtml"
            );

            driver.get(url);

            WebDriverWait wait =
                    new WebDriverWait(driver, Duration.ofSeconds(20));

            driver.get(url);

            Thread.sleep(1000);

            List<WebElement> odds =
                    driver.findElements(
                            By.cssSelector(".oddsCell__odd")
                    );


            System.out.println(
                    "PAGE OPENED"
            );

            wait =
                    new WebDriverWait(driver, Duration.ofSeconds(20));

            wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("span.wcl-oddsValue_3e8Cq")
                    )
            );

            odds =
                    driver.findElements(
                            By.cssSelector("span.wcl-oddsValue_3e8Cq")
                    );

            System.out.println(
                    "ODDS FOUND: " + odds.size()
            );

            for(WebElement odd : odds){

                System.out.println(
                        "ODD: " + odd.getText()
                );
            }

            System.out.println(
                    "ODDS FOUND: " + odds.size()
            );

            for(WebElement odd : odds){

                System.out.println(
                        "ODD TEXT: " + odd.getText()
                );
            }

            System.out.println(
                    "ODDS FOUND: " + odds.size()
            );

            if(odds.size() < 3){

                return new String[]{
                        null,
                        null,
                        null
                };
            }

            String home =
                    odds.get(0).getText();

            String draw =
                    odds.get(1).getText();

            String away =
                    odds.get(2).getText();

            return new String[]{
                    home,
                    draw,
                    away
            };

        } catch(Exception e){

            e.printStackTrace();

            return new String[]{
                    null,
                    null,
                    null
            };

        } finally {

            driver.quit();
        }
    }
}