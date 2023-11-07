package server;

import model.Event;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHelper {

    /**
     * Stores the only class instance
     */
    private static DatabaseHelper instance;
    /**
     * Table name for user table
     */
    private final String TABLE_USER = "User";
    /**
     * Column name for user's username
     */
    private final String COL_USERNAME = "username";
    /**
     * Column name for user's password
     */
    private final String COL_PASSWORD = "password";
    /**
     * Column name for user's display name
     */
    private final String COL_DISPLAY_NAME = "display_name";
    /**
     * Column name for user's online status
     */
    private final String COL_ONLINE = "online";
    /**
     * Table name for event table
     */
    private final String TABLE_EVENT = "Event";
    /**
     * Column name for event's identifier
     */
    private final String COL_EVENT_ID = "event_id";
    /**
     * Column name for event's sender
     */
    private final String COL_SENDER = "sender";
    /**
     * Column name for event's opponent
     */
    private final String COL_OPPONENT = "opponent";
    /**
     * Column name for event's status
     */
    private final String COL_STATUS = "status";
    /**
     * Column name for event's move turn
     */
    private final String COL_TURN = "turn";
    /**
     * Column name for event's move cell
     */
    private final String COL_MOVE = "move";
    /**
     * Store the database connection
     */
    private Connection connection;

    /**
     * A private constructor
     */
    private DatabaseHelper() {
        Logger logger = Logger.getLogger(DatabaseHelper.class.getName());
        try {
            //Connect to the database or create a new db file
            String DB_PATH = "jdbc:sqlite:TicTacToe.db";
            connection = DriverManager.getConnection(DB_PATH);
            createTables();
            logger.log(Level.INFO, "Database Has Been Created");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "A SQL Exception Has Occurred", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An Unknown DB Exception Has Occurred", e);
        }

    }

    /**
     * A getter for the singleton class
     *
     * @return An instance of DatabaseHelper class
     */
    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    /**
     * Truncate Database data
     */
    public void truncateTables() throws SQLException {
        //Truncate User table
        String sql = "DELETE FROM " + TABLE_USER + ";";
        connection.createStatement().executeUpdate(sql);

        //Truncate Event table
        sql = "DELETE FROM " + TABLE_EVENT + ";";
        connection.createStatement().executeUpdate(sql);
    }

    /**
     * Create Database Tables if they do not already exist
     */
    private void createTables() throws SQLException {
        //Creating User table
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " ("
                + COL_USERNAME + " TEXT PRIMARY KEY, "
                + COL_PASSWORD + " TEXT, "
                + COL_DISPLAY_NAME + " TEXT, "
                + COL_ONLINE + " TEXT" +
                ");";
        connection.createStatement().executeUpdate(sql);

        //Creating Event table
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENT + " ("
                + COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SENDER + " TEXT, "
                + COL_OPPONENT + " TEXT, "
                + COL_STATUS + " TEXT, "
                + COL_TURN + " TEXT, "
                + COL_MOVE + " INTEGER, "
                + "FOREIGN KEY(" + COL_SENDER + ") REFERENCES " + TABLE_USER + "(" + COL_USERNAME + "),"
                + "FOREIGN KEY(" + COL_OPPONENT + ") REFERENCES " + TABLE_USER + "(" + COL_USERNAME + ")"
                + ");";
        connection.createStatement().executeUpdate(sql);
    }

    /**
     * Checks if username already exists in the database
     *
     * @param username The username to check if it exists
     * @return true if the user exists in the database, else otherwise
     * @throws SQLException if database error occurs
     */
    public boolean isUsernameExists(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT " + COL_USERNAME
                + " FROM " + TABLE_USER
                + " WHERE " + COL_USERNAME
                + " = ?;");

        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        return rs.next();
    }

    /**
     * Add new user to the database
     *
     * @param user the user to add to the database
     * @throws SQLException if database error occurs
     */
    public void createUser(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + TABLE_USER +
                        "(" + COL_USERNAME + "," + COL_PASSWORD + "," + COL_DISPLAY_NAME + "," + COL_ONLINE + ") " +
                        "VALUES(?, ?, ?, ?);");
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPassword());
        statement.setString(3, user.getDisplayName());
        statement.setBoolean(4, user.isOnline());
        statement.executeUpdate();
    }

    /**
     * Gets the full user details given a username
     *
     * @param username the username of the user
     * @return An object of {@link User} class
     * @throws SQLException if database error occurs
     */
    public User getUser(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_USER
                        + " WHERE " + COL_USERNAME + " = ?;"
        );

        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new User(
                    rs.getString(COL_USERNAME),
                    rs.getString(COL_PASSWORD),
                    rs.getString(COL_DISPLAY_NAME),
                    rs.getBoolean(COL_ONLINE)
            );
        } else {
            return null;
        }
    }

    /**
     * Updates the all user details except for username
     *
     * @param user The updated object of {@link User} class
     * @throws SQLException if database error occurs
     */
    public void updateUser(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + TABLE_USER + " SET "
                        + COL_PASSWORD + " = ?, "
                        + COL_DISPLAY_NAME + " = ?, "
                        + COL_ONLINE + " = ? " +
                        "WHERE " + COL_USERNAME + " = ?;"
        );
        statement.setString(1, user.getPassword());
        statement.setString(2, user.getDisplayName());
        statement.setBoolean(3, user.isOnline());
        statement.setString(4, user.getUsername());
        statement.executeUpdate();
    }

    /**
     * Get list of all users that are available to play a game. That is when:
     * 1. When {@link User#isOnline()} is true
     * 2. Users that do not have an {@link Event} with status:
     * - {@link Event.EventStatus#PLAYING}
     * - {@link Event.EventStatus#ACCEPTED}
     *
     * @param username the username of the user looking for available users
     * @return a list of {@link User}
     * @throws SQLException if database error occurs
     */
    public List<User> getAvailableUsers(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_USER
                        + " WHERE " + COL_USERNAME + " != ?"
                        + " AND " + COL_ONLINE + " = ?"
                        + " AND (SELECT COUNT() FROM " + TABLE_EVENT
                        + " WHERE (" + COL_OPPONENT + " = " + COL_USERNAME
                        + " OR " + COL_SENDER + " = " + COL_USERNAME + ") "
                        + " AND " + COL_STATUS + " IN (?,?)"
                        + ") = ?;"
        );

        statement.setString(1, username);
        statement.setBoolean(2, true);
        statement.setString(3, Event.EventStatus.PLAYING.name());
        statement.setString(4, Event.EventStatus.ACCEPTED.name());
        statement.setInt(5, 0);
        ResultSet rs = statement.executeQuery();
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            users.add(new User(
                    rs.getString(COL_USERNAME),
                    "",//Hidden
                    rs.getString(COL_DISPLAY_NAME),
                    rs.getBoolean(COL_ONLINE)
            ));
        }
        return users;
    }

    /**
     * Check if a user is available to play a game.
     * That is when user do not have an {@link Event} with status:
     * - {@link Event.EventStatus#PLAYING}
     * - {@link Event.EventStatus#ACCEPTED}
     *
     * @param username the username of the user to check for availability
     * @return true if user is available, otherwise false
     * @throws SQLException if database error occurs
     */
    public boolean isUserAvailable(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_EVENT
                        + " WHERE (" + COL_OPPONENT + " = ? OR " + COL_SENDER + " = ?) "
                        + " AND " + COL_STATUS + " IN (?,?);"
        );

        statement.setString(1, username);
        statement.setString(2, username);
        statement.setString(3, Event.EventStatus.PLAYING.name());
        statement.setString(4, Event.EventStatus.ACCEPTED.name());
        ResultSet rs = statement.executeQuery();
        return !rs.next();
    }

    /**
     * Creates a new event in the database
     *
     * @param event The event to create
     * @throws SQLException if database error occurs
     */
    public void createEvent(Event event) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(("INSERT INTO " + TABLE_EVENT
                + "(" + COL_SENDER + "," + COL_OPPONENT + ","
                + COL_STATUS + "," + COL_TURN + "," + COL_MOVE + ") " +
                "VALUES(?, ?, ?, ?, ?);"));
        statement.setString(1, event.getSender());
        statement.setString(2, event.getOpponent());
        statement.setString(3, event.getStatus().name());
        statement.setString(4, event.getTurn());
        statement.setInt(5, event.getMove());
        statement.executeUpdate();
    }

    /**
     * Gets a event given a eventId
     *
     * @param eventId The eventId of the event
     * @return and object of {@link Event} class
     * @throws SQLException if database error occurs
     */
    public Event getEvent(int eventId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_EVENT
                        + " WHERE " + COL_EVENT_ID + " = ?;"
        );

        statement.setInt(1, eventId);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new Event(
                    rs.getInt(COL_EVENT_ID),
                    rs.getString(COL_SENDER),
                    rs.getString(COL_OPPONENT),
                    Event.EventStatus.valueOf(rs.getString(COL_STATUS)),
                    rs.getString(COL_TURN),
                    rs.getInt(COL_MOVE)
            );
        } else {
            return null;
        }
    }

    /**
     * Updates an event. All attributes are updated except eventId
     *
     * @param event The event to update
     * @throws SQLException if database error occurs
     */
    public void updateEvent(Event event) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + TABLE_EVENT + " SET "
                        + COL_STATUS + " = ?, "
                        + COL_TURN + " = ?, "
                        + COL_MOVE + " = ? " +
                        "WHERE " + COL_EVENT_ID + " = ?;"
        );
        statement.setString(1, event.getStatus().name());
        statement.setString(2, event.getTurn());
        statement.setInt(3, event.getMove());
        statement.setInt(4, event.getEventId());
        statement.executeUpdate();
    }

    /**
     * Aborts all event that are not completed.
     * That is event with {@link Event.EventStatus} equal to:
     * - {@link Event.EventStatus#PENDING}
     * - {@link Event.EventStatus#ACCEPTED}
     * - {@link Event.EventStatus#DECLINED}
     * - {@link Event.EventStatus#PLAYING}
     *
     * @param username The username of the user
     * @throws SQLException if database error occurs
     */
    public void abortAllUserEvents(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + TABLE_EVENT +
                        " SET " + COL_STATUS + " = ?" +
                        " WHERE (" + COL_SENDER + " = ?" +
                        " OR " + COL_OPPONENT + " = ?)" +
                        " AND " + COL_STATUS + " IN (?,?,?,?);"
        );
        statement.setString(1, Event.EventStatus.ABORTED.name());
        statement.setString(2, username);
        statement.setString(3, username);
        statement.setString(4, Event.EventStatus.PENDING.name());
        statement.setString(5, Event.EventStatus.ACCEPTED.name());
        statement.setString(6, Event.EventStatus.DECLINED.name());
        statement.setString(7, Event.EventStatus.PLAYING.name());
        statement.executeUpdate();
    }

    /**
     * Gets game invitation sent to a user (i.e {@link Event#getOpponent()})
     *
     * @param username the username of the user
     * @return The event of the invitation
     * @throws SQLException if database error occurs
     */
    public Event getUserInvitation(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_EVENT
                        + " WHERE " + COL_OPPONENT + " = ?"
                        + " AND " + COL_STATUS + " = ?;"
        );

        statement.setString(1, username);
        statement.setString(2, Event.EventStatus.PENDING.name());
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new Event(
                    rs.getInt(COL_EVENT_ID),
                    rs.getString(COL_SENDER),
                    rs.getString(COL_OPPONENT),
                    Event.EventStatus.valueOf(rs.getString(COL_STATUS)),
                    rs.getString(COL_TURN),
                    rs.getInt(COL_MOVE)
            );
        }
        return null;
    }

    /**
     * Gets game invitation response earlier sent by a user (i.e {@link Event#getSender()})
     *
     * @param username the username of the user
     * @return The event of the invitation response
     * @throws SQLException if database error occurs
     */
    public Event getUserInvitationResponse(String username) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_EVENT
                        + " WHERE " + COL_SENDER + " = ?"
                        + " AND " + COL_STATUS + " IN (?,?);"
        );

        statement.setString(1, username);
        statement.setString(2, Event.EventStatus.ACCEPTED.name());
        statement.setString(3, Event.EventStatus.DECLINED.name());
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return new Event(
                    rs.getInt(COL_EVENT_ID),
                    rs.getString(COL_SENDER),
                    rs.getString(COL_OPPONENT),
                    Event.EventStatus.valueOf(rs.getString(COL_STATUS)),
                    rs.getString(COL_TURN),
                    rs.getInt(COL_MOVE)
            );
        }
        return null;
    }
}