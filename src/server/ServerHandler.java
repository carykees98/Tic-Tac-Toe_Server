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
    private final Socket m_Socket;
    private final Gson m_Gson;
    private int m_currentEventId;
    private String m_Username;
    private DataInputStream m_DataIn;
    private DataOutputStream m_DataOut;

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
     * Receives incoming requests and passes them off to handler
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
     * Updated for Task 3.2.18
     */
    public void close() {
        try {
            if (m_Username != null) {
                User user = DatabaseHelper.getInstance().getUser(m_Username);
                if (user != null) {
                    user.setOnlineStatus(false);
                    DatabaseHelper.getInstance().updateUser(user);
                    DatabaseHelper.getInstance().abortAllUserEvents(m_Username);
                }
            }
            m_DataIn.close();
            m_DataOut.close();
            m_Socket.close();
        } catch (SQLException | IOException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
        }
    }


    /**
     * Handles incoming requests from the client
     *
     * @param request request sent by client
     * @return response to the client
     */
    private Response handleRequest(Request request) {
        User user;
        switch (request.getType()) {
            case SEND_MOVE:
                String move = request.getData();
                try {
                    int moveValue = Integer.parseInt(move);
                    return handleSendMove(moveValue);
                } catch (NumberFormatException e) {
                    SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
                    return new Response(ResponseStatus.FAILURE, "Failed to handle move");
                }
            case REQUEST_MOVE:
                return handleRequestMove();
            case LOGIN:
                user = m_Gson.fromJson(request.getData(), User.class);
                return handleLogin(user);
            case REGISTER:
                user = m_Gson.fromJson(request.getData(), User.class);
                return handleRegister(user);
            case UPDATE_PAIRING:
                return handleUpdatePairing();
            case SEND_INVITATION:
                user = m_Gson.fromJson(request.getData(), User.class);
                return handleSendInvitation(user.getUsername());
            case ACCEPT_INVITATION:
                return handleAcceptInvitation(m_currentEventId);
            case DECLINE_INVITATION:
                return handleDeclineInvitation(m_currentEventId);
            case ACKNOWLEDGE_RESPONSE:
                return handleAcknowledgeResponse(m_currentEventId);
            case ABORT_GAME:
                return handleAbortGame();
            case COMPLETE_GAME:
                return handleCompleteGame();
            default:
                return new Response(Response.ResponseStatus.FAILURE, "Unknown request type");
        }
    }

    /**
     * Handles `Request` of type `UPDATE_PAIRING`
     *
     * @return PairingResponse
     */
    private PairingResponse handleUpdatePairing() {
        if (m_Username == null)
            return new PairingResponse(ResponseStatus.FAILURE, "User not logged in", null, null, null);

        try {
            Event invitation = DatabaseHelper.getInstance().getUserInvitation(m_Username);
            Event invitationResponse = DatabaseHelper.getInstance().getUserInvitationResponse(m_Username);

            if (invitation != null) m_currentEventId = invitation.getEventId();

            return new PairingResponse(ResponseStatus.SUCCESS, "Success", DatabaseHelper.getInstance().getAvailableUsers(m_Username), invitation, invitationResponse);
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            return new PairingResponse(ResponseStatus.FAILURE, "Database Action Failed", null, null, null);
        }
    }

    /**
     * Handles `Request` of type `REGISTER`
     *
     * @param user User to Register
     * @return Response
     */
    private Response handleRegister(User user) {
        Response result;
        try {
            if (DatabaseHelper.getInstance().isUsernameExists(user.getUsername())) {
                result = new Response(ResponseStatus.FAILURE, "User already Exists");
            } else {
                DatabaseHelper.getInstance().createUser(user);
                SocketServer.s_Logger.log(Level.INFO, DatabaseHelper.getInstance().getUser(user.getUsername()).getUsername());
                result = new Response(ResponseStatus.SUCCESS, "User Created");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to create new user");
        }
        return result;
    }

    /**
     * Handles `Request` of type `LOGIN`
     *
     * @param user User to Login
     * @return Response
     */
    private Response handleLogin(User user) {
        Response result;
        try {
            SocketServer.s_Logger.log(Level.INFO, user.getUsername());
            User returnedUser = DatabaseHelper.getInstance().getUser(user.getUsername());

            SocketServer.s_Logger.log(Level.INFO, Boolean.toString(returnedUser == null));

            if (returnedUser != null) System.out.println(returnedUser.getPassword() + " " + (user.getPassword()));

            if (returnedUser != null && returnedUser.getPassword().equals(user.getPassword())) {
                System.out.println("Logged in user:" + user.getUsername());
                m_Username = returnedUser.getUsername();

                returnedUser.setOnlineStatus(true);
                DatabaseHelper.getInstance().updateUser(returnedUser);
                System.out.println(DatabaseHelper.getInstance().getUser(m_Username).isOnline());
                result = new Response(ResponseStatus.SUCCESS, "Successfully Logged in");
            } else {
                result = new Response(ResponseStatus.FAILURE, "Failed to fetch user");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to log user in");
        }
        return result;
    }


    /**
     * Handles `Request` of type `SEND_MOVE`
     *
     * @param move move made by client
     * @return Response to be sent to the client
     */
    private Response handleSendMove(int move) {
        Response result;
        try {
            Event currentEvent = DatabaseHelper.getInstance().getEvent(m_currentEventId);
            if (currentEvent.getTurn() == null || !currentEvent.getTurn().equals(m_Username)) {
                currentEvent.setMove(move);
                currentEvent.setTurn(m_Username);
                DatabaseHelper.getInstance().updateEvent(currentEvent);
                result = new Response(ResponseStatus.SUCCESS, "Move accepted");
            } else {
                result = new Response(ResponseStatus.FAILURE, "It's not your turn to make a move");
            }
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "It's not your turn to make a move");
        }
        return result;
    }

    /**
     * Handles `Request` of type `SEND_INVITATION`
     *
     * @param opponent Opponent to send invite to
     * @return Response
     */
    private Response handleSendInvitation(String opponent) {
        Response result;
        try {
            if (m_Username == null) {
                result = new Response(ResponseStatus.FAILURE, "User not logged in");
            } else if (!DatabaseHelper.getInstance().isUserAvailable(opponent)) {
                result = new Response(ResponseStatus.FAILURE, "Opponent is not available to receive an invitation");
            } else {
                Event event = new Event(m_currentEventId, m_Username, opponent, Event.EventStatus.PENDING, null, -1);
                DatabaseHelper.getInstance().createEvent(event);
                m_currentEventId = DatabaseHelper.getInstance().getUserInvitation(opponent).getEventId();
                result = new Response(ResponseStatus.SUCCESS, "Invitation sent to " + opponent);
            }
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to send invitation");
        }
        return result;
    }

    /**
     * Handles `Request` of type `ACCEPT_INVITATION`
     *
     * @param eventId ID of Invitation
     * @return Response
     */
    private Response handleAcceptInvitation(int eventId) {
        Response result;
        try {
            Event event = DatabaseHelper.getInstance().getEvent(eventId);

            if (event == null || event.getStatus() != Event.EventStatus.PENDING || !event.getOpponent().equals(m_Username)) {
                result = new Response(ResponseStatus.FAILURE, "Invalid invitation or not your invitation to accept");
            } else {
                event.setStatus(Event.EventStatus.ACCEPTED);
                DatabaseHelper.getInstance().abortAllUserEvents(m_Username);
                DatabaseHelper.getInstance().updateEvent(event);
                m_currentEventId = eventId;
                result = new Response(ResponseStatus.SUCCESS, "Invitation accepted");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to accept invitation");
        }
        return result;
    }

    /**
     * Handles `Request` of type `DECLINE_INVITATION`
     *
     * @param eventId ID of invitation
     * @return Response
     */
    /*
    Task 14
     */
    private Response handleDeclineInvitation(int eventId) {
        Response result;
        try {
            Event event = DatabaseHelper.getInstance().getEvent(eventId);

            System.out.println(eventId);

            if (event == null || event.getStatus() != Event.EventStatus.PENDING || !event.getOpponent().equals(m_Username)) {
                result = new Response(ResponseStatus.FAILURE, "Invalid invitation or not your invitation to decline");
            } else {
                event.setStatus(Event.EventStatus.DECLINED);
                DatabaseHelper.getInstance().updateEvent(event);
                result = new Response(ResponseStatus.SUCCESS, "Invitation declined");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to decline invitation");
        }
        return result;
    }

    /**
     * Handles `Request` of type `ACKNOWLEDGE_RESPONSE`
     *
     * @param eventId ID of response
     * @return Response
     */
    /*
    Task 15
     */
    private Response handleAcknowledgeResponse(int eventId) {
        Response result;
        try {
            System.out.println(eventId);
            Event event = DatabaseHelper.getInstance().getEvent(eventId);
            System.out.println(event == null);

            if (event == null || !event.getSender().equals(m_Username)) {
                result = new Response(ResponseStatus.FAILURE, "Invalid event or not your response to acknowledge");
            } else {
                if (event.getStatus() == Event.EventStatus.DECLINED) {
                    event.setStatus(Event.EventStatus.ABORTED);
                } else if (event.getStatus() == Event.EventStatus.ACCEPTED) {
                    DatabaseHelper.getInstance().abortAllUserEvents(m_Username);
                    m_currentEventId = eventId;
                }
                DatabaseHelper.getInstance().updateEvent(event);
                result = new Response(ResponseStatus.SUCCESS, "Response acknowledged");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to acknowledge response");
        }
        return result;
    }

    /**
     * Handles `Request` of type `COMPLETE_GAME`
     *
     * @return Response
     */
    private Response handleCompleteGame() {
        Response result;
        try {
            Event event = DatabaseHelper.getInstance().getEvent(m_currentEventId);

            if (event == null || event.getStatus() != Event.EventStatus.PLAYING) {
                result = new Response(ResponseStatus.FAILURE, "Invalid event or not a game in progress");
            } else {
                event.setStatus(Event.EventStatus.COMPLETED);
                DatabaseHelper.getInstance().updateEvent(event);
                m_currentEventId = -1;
                result = new Response(ResponseStatus.SUCCESS, "Game completed");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to mark game complete");
        }
        return result;
    }

    /**
     * Handles `Request` of type `ABORT_GAME`
     *
     * @return Response
     */
    private Response handleAbortGame() {
        Response result;
        try {
            Event event = DatabaseHelper.getInstance().getEvent(m_currentEventId);


            if (event == null || event.getStatus() != Event.EventStatus.PLAYING) {
                result = new Response(ResponseStatus.FAILURE, "Invalid event or not a game in progress");
            } else {
                event.setStatus(Event.EventStatus.ABORTED);
                DatabaseHelper.getInstance().updateEvent(event);
                m_currentEventId = -1;
                result = new Response(ResponseStatus.SUCCESS, "Game aborted");
            }

        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new Response(ResponseStatus.FAILURE, "Failed to abort game");
        }
        return result;
    }


    /**
     * Handles `Request` of type `REQUEST_MOVE`
     *
     * @return GamingResponse
     */
    /*
    Task 17
     */
    private GamingResponse handleRequestMove() {
        GamingResponse result;
        try {
            Event currentEvent = DatabaseHelper.getInstance().getEvent(m_currentEventId);

            boolean active = true;
            String message = "Opponent's move: ";

            if (currentEvent.getStatus() == Event.EventStatus.ABORTED) {
                active = false;
                message = "Opponent Abort";
            } else if (currentEvent.getStatus() == Event.EventStatus.COMPLETED) {
                active = false;
                message = "Opponent Deny Play Again";
            }

            if (currentEvent.getMove() != -1 && !currentEvent.getTurn().equals(m_Username)) {
                int move = currentEvent.getMove();
                currentEvent.setMove(-1);
                currentEvent.setTurn("");
                DatabaseHelper.getInstance().updateEvent(currentEvent);
                result = new GamingResponse(ResponseStatus.SUCCESS, message + move, move, active);
            } else {
                result = new GamingResponse(ResponseStatus.SUCCESS, "No move from the opponent yet", -1, active);
            }
        } catch (SQLException e) {
            SocketServer.s_Logger.log(Level.SEVERE, e.getMessage());
            result = new GamingResponse(ResponseStatus.FAILURE, "Failed to retrieve move", -1, true);
        }
        return result;
    }


}

