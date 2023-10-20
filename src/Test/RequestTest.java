package Test;

import org.junit.Test;
import socket.Request;

import static org.junit.Assert.assertEquals;

public class RequestTest {
    Request testRequest = new Request(Request.RequestType.LOGIN, "LoginData");

    @Test
    public void getType() {
        assertEquals(Request.RequestType.LOGIN, testRequest.getType());
    }

    @Test
    public void setType() {
        testRequest.setType(Request.RequestType.REGISTER);
        assertEquals(Request.RequestType.REGISTER, testRequest.getType());
    }

    @Test
    public void getData() {
        assertEquals("LoginData", testRequest.getData());
    }
}