package test;

import org.junit.Test;
import socket.Response;
import socket.Response.ResponseStatus;

import static org.junit.Assert.*;

public class ResponseTest {
    @Test
    public void getMessage() {
        Response response = new Response(ResponseStatus.SUCCESS, "Test Message");
        assertEquals("Test Message", response.getMessage());
    }

    @Test
    public void getStatus() {
        Response response = new Response(ResponseStatus.FAILURE, "Test Message");
        assertEquals(ResponseStatus.FAILURE, response.getStatus());
    }
}