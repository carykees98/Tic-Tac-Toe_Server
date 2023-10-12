package server;

public class SocketServer {
    private final int m_Port;

    SocketServer() {
        m_Port = 5000;
    }

    SocketServer(int port) {
        m_Port = port;
    }

    public static void main(String[] args) {
        SocketServer server = new SocketServer();
        server.setup();
        server.startAcceptingRequest();
    }

    public void setup() {
    }

    public void startAcceptingRequest() {

    }

    public int getPort() {
        return m_Port;
    }
}
