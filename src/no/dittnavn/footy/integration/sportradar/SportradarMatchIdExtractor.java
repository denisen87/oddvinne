package no.dittnavn.footy.integration.sportradar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SportradarMatchIdExtractor {

    public Set<Integer> extractMatchIds(String url) {

        Set<Integer> ids =
                new HashSet<>();

        try {

            ChromeDriver driver =
                    new ChromeDriver();

            driver.get(url);

            Thread.sleep(10000);

            // ===== FINN IFRAME =====

            WebElement iframe =
                    driver.findElement(
                            By.id("sportsbookid")
                    );

            // ===== GÅ INN I IFRAME =====

            driver.switchTo().frame(iframe);

            Thread.sleep(15000);

            driver.navigate().refresh();

            Thread.sleep(10000);

            String html =
                    driver.getPageSource();

            System.out.println(html.length());

            System.out.println(
                    html.contains("match_details")
            );

            Pattern pattern =
                    Pattern.compile(
                            "match_details/(\\d+)"
                    );

            Matcher matcher =
                    pattern.matcher(html);

            while (matcher.find()) {

                int id =
                        Integer.parseInt(
                                matcher.group(1)
                        );

                System.out.println(
                        "Fant id: " + id
                );

                ids.add(id);
            }

            driver.quit();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return ids;
    }
}