package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Event;
import socket.GamingResponse;
import socket.Request;
import socket.Response;
import socket.Response.ResponseStatus;

import javax.tools.JavaFileObject;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that handles communication with the SQLite db
 */
public class ServerHandler extends Thread {
    private static Event s_Event = new Event(0, null, null, null, null, -1);
    private Socket m_Socket;
    private String m_Username;
    private DataInputStream m_DataIn;
    private DataOutputStream m_DataOut;
    private Gson m_Gson;

    /**
     * @param socket   socket connection to the client
     * @param username username of the connected client
     */
    public ServerHandler(Socket socket, String username) {
        m_Socket = socket;
        m_Username = username;
        m_Gson = new GsonBuilder().serializeNulls().create();

        try {
            m_DataIn = new DataInputStream(socket.getInputStream());
            m_DataOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            SocketServer.s_Logger.log(Level.SEVERE, "Failed to open data stream");
        }

        SocketServer.s_Logger.log(Level.INFO, "Connected:" + username);
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
        if (s_Event.getTurn() == null || !s_Event.getTurn().equals(m_Username)) {
            s_Event.setLastMove(move);
            s_Event.setTurn(m_Username);
            return new Response(ResponseStatus.SUCCESS, "Move accepted");
        } else {
            return new Response(ResponseStatus.FAILURE, "It's not your turn to make a move");
        }
    }

    /**
     * Handles request from client for a new move
     *
     * @return response with the most recent move data
     */
    private GamingResponse handleRequestMove() {
        if (s_Event.getLastMove() != -1 && !s_Event.getTurn().equals(m_Username)) {
            int move = s_Event.getLastMove();
            s_Event.setLastMove(-1);
            return new GamingResponse(ResponseStatus.SUCCESS, "Opponent's move: " + move, move);
        } else {
            return new GamingResponse(ResponseStatus.SUCCESS, "No move from the opponent yet", -1);
        }
    }
}
