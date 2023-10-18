package Test;

import org.junit.Test;

import static org.junit.Assert.*;

import socket.Response;

public class ResponseTest {
    private Response testResponse = new Response(ResponseStatus.SUCCESS, "Howdy!");

    @Test
    public void getMessage() {
        String messaged = testResponse.getMessage();
        assertEquals("Howdy!", message);
    }

    @Test
    public void getStatus() {
        ResponseStatus status = testResponse.getStatus();
        assertEquals(ResponseStatus.Success,status);
    }
}