package no.dittnavn.footy.integration.sportradar;
// norsktipping

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.dittnavn.footy.integration.playars.PlayerDAO;

import no.dittnavn.footy.integration.playars.Player;

import java.sql.Connection;
import java.sql.DriverManager;

public class SportradarTest {

    public static void main(String[] args) {

        SportradarClient client =
                new SportradarClient();

        String json =
                client.fetch("stats_team_squad/6111");

        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(json);

            JsonNode players =
                    root.get("doc")
                            .get(0)
                            .get("data")
                            .get("players");

            Connection connection =
                    DriverManager.getConnection(
                            "jdbc:sqlite:betting.db"
                    );

            PlayerDAO dao =
                    new PlayerDAO(connection);

            for (JsonNode playerNode : players) {

                String name =
                        playerNode.path("name").asText();

                String number =
                        playerNode.path("shirtnumber").asText();

                String nationality =
                        playerNode.path("nationality")
                                .path("name")
                                .asText();

                String position =
                        playerNode.path("position")
                                .path("name")
                                .asText();

                String foot =
                        playerNode.path("foot")
                                .asText();

                int height =
                        playerNode.path("height")
                                .asInt();

                int weight =
                        playerNode.path("weight")
                                .asInt();

                String birthDate =
                        playerNode.path("birthdate")
                                .path("date")
                                .asText();

                Player player =
                        new Player(
                                name,
                                nationality,
                                position,
                                foot,
                                height,
                                weight,
                                birthDate,
                                number,
                                "Tolima"

                        );

                System.out.println(player.getName());

                dao.save(player);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}