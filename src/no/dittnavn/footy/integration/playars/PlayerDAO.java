package no.dittnavn.footy.integration.playars;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class PlayerDAO {

    private final Connection connection;

    public PlayerDAO(Connection connection) {

        this.connection = connection;

        createTable();
    }

    private void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS players (

                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    name TEXT,
                    nationality TEXT,
                    position TEXT,
                    foot TEXT,

                    height INTEGER,
                    weight INTEGER,

                    birthdate TEXT,
                    shirt_number TEXT,

                    team TEXT
                )
                """;

        try {

            Statement stmt =
                    connection.createStatement();

            stmt.execute(sql);

            System.out.println(
                    "Players table ready."
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void save(Player player) {

        String sql = """

                INSERT INTO players (

                    name,
                    nationality,
                    position,
                    foot,

                    height,
                    weight,

                    birthdate,
                    shirt_number,

                    team
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)

                """;

        try {

            PreparedStatement stmt =
                    connection.prepareStatement(sql);

            stmt.setString(1, player.getName());

            stmt.setString(2, player.getNationality());

            stmt.setString(3, player.getPosition());

            stmt.setString(4, player.getFoot());

            stmt.setInt(5, player.getHeight());

            stmt.setInt(6, player.getWeight());

            stmt.setString(7, player.getBirthdate());

            stmt.setString(8, player.getShirtNumber());

            stmt.setString(9, player.getTeam());

            stmt.executeUpdate();

            System.out.println(
                    "Saved player: " +
                            player.getName()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}