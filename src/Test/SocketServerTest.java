package Test;

import org.junit.Test;

import static org.junit.Assert.*;

import server.SocketServer;

public class SocketServerTest {
    private SocketServer server = new SocketServer(5000);
    @Test
    public void setup() {
        server.setup();
        assertNotNull(server.getServerSocket());
        assertEquals(5000, server.getPort());
    }

    @Test
    public void startAcceptingRequest() {
        server.setup();
        server.startAcceptingRequest();
        assertTrue(server.isListening());
    }
    @Test
    public void getPort() {
        assertEquals(5000, server.getPort());
    }
}