package Test;

import org.junit.Test;
import server.SocketServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SocketServerTest {
    private final SocketServer server = new SocketServer(5000);

    @Test
    public void setup() {
        server.setup();
        assertNotNull(server.getPort());
        assertEquals(5000, server.getPort());
    }

    @Test
    public void startAcceptingRequest() {
        server.setup();
        server.startAcceptingRequest();
    }

    @Test
    public void getPort() {
        assertEquals(5000, server.getPort());
    }
}