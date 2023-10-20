package Test;

import org.junit.Test;
import socket.Response;

import static org.junit.Assert.assertEquals;

public class ResponseTest {
    private final Response testResponse = new Response(Response.ResponseStatus.SUCCESS, "Howdy!");

    @Test
    public void getMessage() {
        String message = testResponse.getMessage();
        assertEquals("Howdy!", message);
    }

    @Test
    public void getStatus() {
        Response.ResponseStatus status = testResponse.getStatus();
        assertEquals(Response.ResponseStatus.SUCCESS, status);
    }
}