package test;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.SocketServer;
import socket.*;

/**
 * This class helps in creating socket client for testing purpose
 */
public class SocketClientHelper {

    /**
     * Used for printing server logs of different levels
     */
    private final Logger LOGGER;

    /**
     * Used for object serialization
     */
    private final Gson gson;

    /**
     * Socket connection with the server
     */
    private Socket socket;

    /**
     * Stream used to read (receive) server response to our request
     */
    private DataInputStream inputStream;

    /**
     * Stream used to write (send) our request to the server
     */
    private DataOutputStream outputStream;


    /**
     * A private constructor that instantiate the class and set attributes
     * Can be accessed only the within the class (for singleton design pattern)
     */
    public SocketClientHelper() {
        String HOSTNAME = "0.0.0.0";
        int PORT = 5000;

        LOGGER = Logger.getLogger(SocketServer.class.getName());
        gson = new GsonBuilder().serializeNulls().create();

        try {
            socket = new Socket(InetAddress.getByName(HOSTNAME), PORT);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     *
     * @param request The request to be sent to the server
     * @param responseClass The class of response we expect from the server
     * @return Object of the responseClass received from the server
     * @param <T> {@link Response} class or one of its subclasses i.e., {@link GamingResponse} and {@link PairingResponse}
     */
    public <T> T sendRequest(Request request, Class<T> responseClass) {
        try {
            // Send Request
            String serializedRequest = gson.toJson(request);
            outputStream.writeUTF(serializedRequest);
            outputStream.flush();

            // Get Response
            String serializedResponse = inputStream.readUTF();
            return gson.fromJson(serializedResponse, responseClass);
        } catch (IOException e) {
            close();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Closes the socket connection with the server and all IO Streams
     * Destruct the singleton instance
     */
    public void close() {
        try {
            if(socket != null) socket.close();
            if(inputStream != null) inputStream.close();
            if(outputStream != null) outputStream.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}