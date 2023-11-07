package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Event;
import socket.GamingResponse;
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
     */
    public void close() {
        try {
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
}
