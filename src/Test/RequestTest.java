package Test;

import org.junit.Test;
import socket.Request;

import static org.junit.Assert.*;

public class RequestTest {
    private Request request;
    @Test
    public void getType() {
        Request.RequestType expectedType = Request.RequestType.LOGIN;
        Request request = new Request(expectedType, "Test Data");
        assertEquals(expectedType, request.getType());
    }

    @Test
    public void setType() {
        Request request = new Request();
        Request.RequestType newType = Request.RequestType.REGISTER;
        request.setType(newType);
        assertEquals(newType, request.getType());
    }

    @Test
    public void getData() {
        String expectedData = "Test Data";
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, expectedData);
        assertEquals(expectedData, request.getData());
    }
}