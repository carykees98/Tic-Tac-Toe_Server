package Test;

import org.junit.Test;

import static org.junit.Assert.*;

import socket.Request;

public class RequestTest {
    Request testRequest = new request(Request.RequestType.LOGIN, "LoginData");
    @Test
    public void getType() {
        assertEquals(RequestType.LOGIN, testRequest.getType());
    }

    @Test
    public void setType(){
        request.setType(RequestType.REGISTER);
        assertEquals(RequestType.REGISTER,request.getType());
    }

    @Test
    public void getData() {
        assertEquals("LoginData", request.getData());
    }
}