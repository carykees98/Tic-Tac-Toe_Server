package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Event;
import model.User;
import socket.GamingResponse;
import socket.PairingResponse;
import socket.Request;
import socket.Response;
import socket.Response.ResponseStatus;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Class that handles communication with the SQLite db
 */
public class ServerHandler extends Thread {
    private int m_currentEventId;
    private Socket m_Socket;
    private String m_Username;
    private DataInputStream m_DataIn;
    private DataOutputStream m_DataOut;
    private Gson m_Gson;

    /**
     * @param socket socket connection to the client
     */
    public ServerHandler(Socket socket) {
        m_Socket = socket;
        m_Gson = new GsonBuilder().serializeNulls().create();

        try {
            m_DataIn = new DataInputStream(socket.getInputStream());
            m_DataOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            SocketServer.s_Logger.log(Level.SEVERE, "Failed to open data stream");
        }
    }

    /**
     * Run creates a handle to the SQLite db
     */
    @Override
    public void run() {
        while (true) {
            try {
                Request request = m_Gson.fromJson(m_DataIn.readUTF(), Request.class);
                String gson = m_Gson.toJson(handleRequest(request));
                m_DataOut.writeUTF(gson);
                SocketServer.s_Logger.log(Level.INFO, "Response: " + gson);
                m_DataOut.flush();

            } catch (EOFException e) {
                close();
            } catch (Exception e) {
                SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    /**
     * terminates the socket connection
     * Updated for Task 18
     */
    public void close() {
        try {
            if (m_Username != null) {
                User user = DatabaseHelper.getInstance().getUser(m_Username);
                if (user != null) {
                    user.setOnlineStatus(false);
                    DatabaseHelper.getInstance().updateUser(user);
                    DatabaseHelper.getInstance().abortAllUserEvents(m_Username, -1);
                }
            }
            m_DataIn.close();
            m_DataOut.close();
            m_Socket.close();
        } catch (IOException ioe) {
            SocketServer.s_Logger.log(Level.SEVERE, "Failed to close socket and streams");
        }
    }


    /**
     * Handles incoming requests from the client
     *
     * @param request request sent by client
     * @return response to the client
     */
    public Response handleRequest(Request request) {
        switch (request.getType()) {
            case SEND_MOVE:
                String move = request.getData();
                try {
                    int moveValue = Integer.parseInt(move);
                    return handleSendMove(moveValue);
                } catch (NumberFormatException e) {
                    SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
                }
            case REQUEST_MOVE:
                return handleRequestMove();
            case LOGIN:
            case REGISTER:
                return handleRegister();
            case UPDATE_PAIRING:
            case SEND_INVITATION:
            case ACCEPT_INVITATION:
            case ACKNOWLEDGE_RESPONSE:
            case ABORT_GAME:
            case COMPLETE_GAME:
            default:
                return new Response(Response.ResponseStatus.FAILURE, "Unknown request type");
        }
    }

    private PairingResponse handleUpdatePairing() {
        if (m_Username == null)
            return new PairingResponse(ResponseStatus.FAILURE,
                    "User not logged in",
                    null,
                    null,
                    null);

        try {
            return new PairingResponse(ResponseStatus.SUCCESS,
                    "Success",
                    DatabaseHelper.getInstance().getAvailableUsers(m_Username),
                    DatabaseHelper.getInstance().getUserInvitation(m_Username),
                    DatabaseHelper.getInstance().getUserInvitationResponse(m_Username));
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new PairingResponse(ResponseStatus.FAILURE,
                    "Database Action Failed",
                    null,
                    null,
                    null);
        }
    }

    private Response handleRegister(User user) {
        try {
            if (DatabaseHelper.getInstance().isUsernameExists(user.getUsername()))
                return new Response(ResponseStatus.FAILURE, "User already Exists");

            DatabaseHelper.getInstance().createUser(user);
            return new Response(ResponseStatus.SUCCESS, "User Created");
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new Response(ResponseStatus.FAILURE, "Failed to create new user");
        }
    }

    private Response handleLogin(User user) {
        try {
            User returnedUser = DatabaseHelper.getInstance().getUser(user.getUsername());

            if (returnedUser != null && returnedUser.getPassword().equals(user.getPassword())) {
                m_Username = returnedUser.getUsername();
                returnedUser.setOnlineStatus(true);
                DatabaseHelper.getInstance().createUser(returnedUser);
                return new Response(ResponseStatus.SUCCESS, "Successfully Logged in");
            } else {
                return new Response(ResponseStatus.FAILURE, "Failed to fetch user");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new Response(ResponseStatus.FAILURE, "Failed to log user in");
        }
    }


    /**
     * Handles move sent by client
     *
     * @param move move made by client
     * @return Response to be sent to the client
     */
    private Response handleSendMove(int move) {
        try {
            Event currentEvent = DatabaseHelper.getInstance().getEvent(m_currentEventId);
            if (currentEvent.getTurn() == null || !currentEvent.getTurn().equals(m_Username)) {
                currentEvent.setMove(move);
                currentEvent.setTurn(m_Username);
                DatabaseHelper.getInstance().updateEvent(currentEvent);
                return new Response(ResponseStatus.SUCCESS, "Move accepted");
            } else {
                return new Response(ResponseStatus.FAILURE, "It's not your turn to make a move");
            }
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new Response(ResponseStatus.FAILURE, "It's not your turn to make a move");
        }
    }

    /**
     * Handles request from client for a new move
     *
     * @return response with the most recent move data
     */
    private GamingResponse handleRequestMove() {
        try {
            Event currentEvent = DatabaseHelper.getInstance().getEvent(m_currentEventId);
            if (currentEvent.getMove() != -1 && !currentEvent.getTurn().equals(m_Username)) {
                int move = currentEvent.getMove();
                currentEvent.setMove(-1);
                currentEvent.setTurn("");
                DatabaseHelper.getInstance().updateEvent(currentEvent);
                return new GamingResponse(ResponseStatus.SUCCESS, "Opponent's move: " + move, move);
            } else {
                return new GamingResponse(ResponseStatus.SUCCESS, "No move from the opponent yet", -1);
            }
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new GamingResponse(ResponseStatus.SUCCESS, "No move from the opponent yet", -1);
        }
    }
    /*
    Task 12
     */
    private Response handleSendInvitation(String opponent) {
        if (m_Username == null) {
            return new Response(ResponseStatus.FAILURE, "User not logged in");
        }

        if (!DatabaseHelper.getInstance().isUserAvailable(opponent)) {
            return new Response(ResponseStatus.FAILURE, "Opponent is not available to receive an invitation");
        }

        Event event = new Event(m_Username, opponent, Event.Status.PENDING, -1);
        DatabaseHelper.getInstance().createEvent(event);
        return new Response(ResponseStatus.SUCCESS, "Invitation sent to " + opponent);
    }

    /*
    Task 13
     */
    private Response handleAcceptInvitation(int eventId) {
        Event event = DatabaseHelper.getInstance().getEvent(eventId);

        if (event == null || event.getStatus() != Event.Status.PENDING || !event.getOpponent().equals(m_Username)) {
            return new Response(ResponseStatus.FAILURE, "Invalid invitation or not your invitation to accept");
        }

        event.setStatus(Event.Status.ACCEPTED);
        DatabaseHelper.getInstance().abortAllUserEvents(m_Username, eventId);
        DatabaseHelper.getInstance().updateEvent(event);
        m_currentEventId = eventId;
        return new Response(ResponseStatus.SUCCESS, "Invitation accepted");
    }

    /*
    Task 14
     */
    private Response handleDeclineInvitation(int eventId) {
        Event event = DatabaseHelper.getInstance().getEvent(eventId);

        if (event == null || event.getStatus() != Event.Status.PENDING || !event.getOpponent().equals(m_Username)) {
            return new Response(ResponseStatus.FAILURE, "Invalid invitation or not your invitation to decline");
        }

        event.setStatus(Event.Status.DECLINED);
        DatabaseHelper.getInstance().updateEvent(event);
        return new Response(ResponseStatus.SUCCESS, "Invitation declined");
    }

    /*
    Task 15
     */
    private Response handleAcknowledgeResponse(int eventId) {
        Event event = DatabaseHelper.getInstance().getEvent(eventId);

        if (event == null || !event.getSender().equals(m_Username)) {
            return new Response(ResponseStatus.FAILURE, "Invalid event or not your response to acknowledge");
        }

        if (event.getStatus() == Event.Status.DECLINED) {
            event.setStatus(Event.Status.ABORTED);
        } else if (event.getStatus() == Event.Status.ACCEPTED) {
            DatabaseHelper.getInstance().abortAllUserEvents(m_Username, eventId);
            m_currentEventId = eventId;
        }

        DatabaseHelper.getInstance().updateEvent(event);
        return new Response(ResponseStatus.SUCCESS, "Response acknowledged");
    }

    /*
    Task 16
     */
    private Response handleCompleteGame() {
        Event event = DatabaseHelper.getInstance().getEvent(m_currentEventId);

        if (event == null || event.getStatus() != Event.Status.PLAYING) {
            return new Response(ResponseStatus.FAILURE, "Invalid event or not a game in progress");
        }

        event.setStatus(Event.Status.COMPLETED);
        DatabaseHelper.getInstance().updateEvent(event);
        m_currentEventId = -1;
        return new Response(ResponseStatus.SUCCESS, "Game completed");
    }

    private Response handleAbortGame() {
        Event event = DatabaseHelper.getInstance().getEvent(m_currentEventId);

        if (event == null || event.getStatus() != Event.Status.PLAYING) {
            return new Response(ResponseStatus.FAILURE, "Invalid event or not a game in progress");
        }

        event.setStatus(Event.Status.ABORTED);
        DatabaseHelper.getInstance().updateEvent(event);
        m_currentEventId = -1;
        return new Response(ResponseStatus.SUCCESS, "Game aborted");
    }


    /*
    Task 17
     */
    private GamingResponse handleRequestMove() {
        try {
            Event currentEvent = DatabaseHelper.getInstance().getEvent(m_currentEventId);

            boolean active = true;
            String message = "Opponent's move: ";

            if (currentEvent.getStatus() == Event.Status.ABORTED) {
                active = false;
                message = "Opponent Abort";
            } else if (currentEvent.getStatus() == Event.Status.COMPLETED) {
                active = false;
                message = "Opponent Deny Play Again";
            }

            if (currentEvent.getMove() != -1 && !currentEvent.getTurn().equals(m_Username)) {
                int move = currentEvent.getMove();
                currentEvent.setMove(-1);
                currentEvent.setTurn("");
                DatabaseHelper.getInstance().updateEvent(currentEvent);
                return new GamingResponse(ResponseStatus.SUCCESS, message + move, move, active);
            } else {
                return new GamingResponse(ResponseStatus.SUCCESS, "No move from the opponent yet", -1, active);
            }
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new GamingResponse(ResponseStatus.FAILURE, "Failed to retrieve move", -1, true);
        }
    }


}

