package server;

/**
 * Wrapper Class for a socket
 */
public class SocketServer {
    private final int m_Port;

    /**
     * Default constructor for the SocketServer class
     */
    SocketServer() {
        m_Port = 5000;
    }

    /**
     * @param port Port for the socket to bind to (between 0 and 65535)
     */
    SocketServer(int port) {
        m_Port = port;
    }

    /**
     * Entry point for the server
     *
     * @param args Command Line Arguments
     */
    public static void main(String[] args) {
        SocketServer server = new SocketServer();
        server.setup();
        server.startAcceptingRequest();
    }

    /**
     * Configures the SocketServer
     */
    public void setup() {
    }

    /**
     * Tells the socket to start accepting requests from clients
     */
    public void startAcceptingRequest() {

    }

    /**
     * Returns the port that the socket is connected to
     *
     * @return Returns the value of m_Port
     */
    public int getPort() {
        return m_Port;
    }
}
