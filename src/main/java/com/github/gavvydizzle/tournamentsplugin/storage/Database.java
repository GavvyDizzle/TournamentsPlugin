package com.github.gavvydizzle.tournamentsplugin.storage;

import com.github.gavvydizzle.tournamentsplugin.TournamentsPlugin;
import com.github.gavvydizzle.tournamentsplugin.tournaments.*;
import com.github.mittenmc.gangsplugin.api.GangsAPI;
import com.github.mittenmc.gangsplugin.gangs.Gang;
import com.github.mittenmc.serverutils.UUIDConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

public abstract class Database {

    private final String CREATE_INDIVIDUAL_TOURNAMENT_TABLE = "CREATE TABLE IF NOT EXISTS {table_name}(" +
            "uuid BINARY(16) NOT NULL," +
            "score BIGINT    NOT NULL," +
            "PRIMARY KEY (uuid)" +
            ");";

    private final String CREATE_GANG_TOURNAMENT_TABLE = "CREATE TABLE IF NOT EXISTS {table_name}(" +
            "gangID INT           NOT NULL," +
            "gangName VARCHAR(16) NOT NULL," +
            "score BIGINT         NOT NULL," +
            "PRIMARY KEY (gangID)" +
            ");";

    private final String LOAD_DATA = "SELECT * FROM {table_name} ORDER BY score DESC;";

    private final String UPSERT_INDIVIDUAL = "INSERT OR REPLACE INTO {table_name}(uuid, score) VALUES(?,?);";

    private final String UPSERT_GANG = "INSERT OR REPLACE INTO {table_name}(gangID, gangName, score) VALUES(?,?,?);";

    private final String DELETE_INDIVIDUAL = "DELETE FROM {table_name} WHERE uuid=?;";

    private final String DELETE_GANG = "DELETE FROM {table_name} WHERE gangID=?;";

    private final String DELETE_TABLE = "DROP TABLE {table_name};";

    TournamentsPlugin plugin;
    Connection connection;
    private final GangsAPI gangsAPI;

    public Database(TournamentsPlugin instance){
        plugin = instance;
        gangsAPI = instance.getGangsAPI();
    }

    public abstract Connection getSQLConnection();

    /**
     * If the table for this tournament does not exist, one will be created
     * @param tournament The IndividualTournament to create
     */
    public void createTournament(IndividualTournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(CREATE_INDIVIDUAL_TOURNAMENT_TABLE.replace("{table_name}", tournament.getId()));
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * If the table for this tournament does not exist, one will be created
     * @param tournament The GangTournament to create
     */
    public void createTournament(GangTournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(CREATE_GANG_TOURNAMENT_TABLE.replace("{table_name}", tournament.getId()));
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Retrieves the data for this IndividualTournament
     * @param tournament The tournament
     * @return A list of sorted participants for this tournament.
     */
    public ArrayList<IndividualParticipant> loadTournament(IndividualTournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        ArrayList<IndividualParticipant> participants = new ArrayList<>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(LOAD_DATA.replace("{table_name}", tournament.getId()));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                participants.add(new IndividualParticipant(UUIDConverter.convert(resultSet.getBytes(1)), resultSet.getLong(2)));
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return participants;
    }

    /**
     * Retrieves the data for this GangTournament
     * @param tournament The tournament
     * @return A list of sorted participants for this tournament.
     */
    public ArrayList<GangParticipant> loadTournament(GangTournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        ArrayList<GangParticipant> participants = new ArrayList<>();
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(LOAD_DATA.replace("{table_name}", tournament.getId()));
            ResultSet resultSet = ps.executeQuery();

            if (tournament.getTimeType() == TournamentTimeType.COMPLETED) {
                while (resultSet.next()) {
                    participants.add(new GangParticipant(resultSet.getInt(1), resultSet.getString(2), resultSet.getLong(3)));
                }
            }
            else {
                assert GangsAPI.getInstance() != null;
                while (resultSet.next()) {
                    Gang gang = gangsAPI.getGangByID(resultSet.getInt(1));
                    if (gang == null) continue;

                    participants.add(new GangParticipant(gang, resultSet.getLong(3)));
                }
            }

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return participants;
    }

    /**
     * Saves all data for this tournament to its database.
     * This method assumes that its database table already exists.
     * @param tournament The IndividualTournament to save.
     */
    public void saveTournament(IndividualTournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_INDIVIDUAL.replace("{table_name}", tournament.getId()));

            for (IndividualParticipant individualParticipant : tournament.getSortedParticipants()) {
                if (individualParticipant == null) continue;

                ps.setBytes(1, UUIDConverter.convert(individualParticipant.getUniqueId()));
                ps.setLong(2, individualParticipant.getScore());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Saves all data for this tournament to its database.
     * This method assumes that its database table already exists.
     * @param tournament The GangTournament to save.
     */
    public void saveTournament(GangTournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_GANG.replace("{table_name}", tournament.getId()));

            for (GangParticipant gangParticipant : tournament.getSortedGangs()) {
                if (gangParticipant == null) continue;

                ps.setInt(1, gangParticipant.getId());
                ps.setString(2, gangParticipant.getGangName());
                ps.setLong(3, gangParticipant.getScore());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Alters a single player's score in the tournament's database
     * @param tournament The tournament
     * @param individualParticipant The participant
     * @param newScore What to set their score to
     */
    public void setScore(Tournament tournament, IndividualParticipant individualParticipant, long newScore) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_INDIVIDUAL.replace("{table_name}", tournament.getId()));
            ps.setBytes(1, UUIDConverter.convert(individualParticipant.getUniqueId()));
            ps.setLong(2, newScore);
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Alters a single gang's score in the tournament's database
     * @param tournament The tournament
     * @param gangParticipant The participant
     * @param newScore What to set their score to
     */
    public void setScore(Tournament tournament, GangParticipant gangParticipant, long newScore) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(UPSERT_GANG.replace("{table_name}", tournament.getId()));
            ps.setInt(1, gangParticipant.getId());
            ps.setString(2, gangParticipant.getGangName());
            ps.setLong(3, newScore);
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Removes a participant from the tournament
     * @param tournament The tournament
     * @param individualParticipant The participant to remove
     */
    public void removeParticipant(Tournament tournament, IndividualParticipant individualParticipant) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(DELETE_INDIVIDUAL.replace("{table_name}", tournament.getId()));
            ps.setBytes(1, UUIDConverter.convert(individualParticipant.getUniqueId()));
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Removes a participant from the tournament
     * @param tournament The tournament
     * @param gangParticipant The gang to remove
     */
    public void removeParticipant(Tournament tournament, GangParticipant gangParticipant) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(DELETE_GANG.replace("{table_name}", tournament.getId()));
            ps.setInt(1, gangParticipant.getId());
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Deletes the table in the database for this tournament
     * @param tournament The tournament's table to delete
     */
    public void deleteTable(Tournament tournament) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(DELETE_TABLE.replace("{table_name}", tournament.getId()));
            ps.execute();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

}