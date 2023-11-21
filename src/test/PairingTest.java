package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Event;
import model.User;
import org.junit.BeforeClass;
import org.junit.Test;
import server.DatabaseHelper;
import server.SocketServer;
import socket.PairingResponse;
import socket.Request;
import socket.Response;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PairingTest {

    private static User user1;
    private static User user2;
    private static User user3;
    private static User user4;
    private static SocketClientHelper clientHelper1;
    private static SocketClientHelper clientHelper2;
    private static SocketClientHelper clientHelper3;
    private static SocketClientHelper clientHelper4;
    private static int currentEventId;

    final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    @BeforeClass
    public static void setUp() {
        Thread mainThread = new Thread(() -> {
            try {
                DatabaseHelper.getInstance().truncateTables();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            SocketServer.main(null);
        });
        mainThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Initialize User objects with unique usernames and display names
        user1 = new User("user1", "passwdName1", "d1", false);
        user2 = new User("user2", "passwdName2", "d2", false);
        user3 = new User("user3", "passwdName3", "d3", false);
        user4 = new User("user4", "passwdName4", "d4", false);

        // Initialize SocketClientHelper objects for each user
        clientHelper1 = new SocketClientHelper();
        clientHelper2 = new SocketClientHelper();
        clientHelper3 = new SocketClientHelper();
        clientHelper4 = new SocketClientHelper();
    }

    /*
     * Test 1. Send a LOGIN request with user1. It should return a FAILURE response since the user is not registered.
     */
    @Test
    public void test1__() {
        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, gson.toJson(user1));
        Response response_u1 = clientHelper1.sendRequest(loginRequest_u1, Response.class);
        System.out.println(gson.toJson(response_u1));

        assertNotNull(response_u1);
        assertEquals(response_u1.getStatus(), Response.ResponseStatus.FAILURE);
    }

    /*
     * Test 2. Send a REGISTER request with user1. It should return a SUCCESS response.
     */
    @Test
    public void test2__() {
        Request registerRequest_u1 = new Request(Request.RequestType.REGISTER, gson.toJson(user1));
        Response response_u1 = clientHelper1.sendRequest(registerRequest_u1, Response.class);
        System.out.println(gson.toJson(response_u1));

        assertNotNull(response_u1);
        assertEquals(response_u1.getStatus(), Response.ResponseStatus.SUCCESS);
    }

    /*
    Test 3. Send another LOGIN request with user1, but set a wrong login password. It should return a FAILURE response.
    */
    @Test
    public void test3__() {
        User tmp_user1 = new User("user1", "ABCD", "d1", false);

        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, gson.toJson(tmp_user1));
        Response response_u1 = clientHelper1.sendRequest(loginRequest_u1, Response.class);
        System.out.println(gson.toJson(response_u1));

        assertNotNull(response_u1);
        assertEquals(response_u1.getStatus(), Response.ResponseStatus.FAILURE);
    }

    /*
     * Test 4. Send another LOGIN request with user1, but this time with the correct password. It should return a SUCCESS response.
     */
    @Test
    public void test4__() {
        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, gson.toJson(user1));
        Response response_u1 = clientHelper1.sendRequest(loginRequest_u1, Response.class);
        System.out.println(gson.toJson(response_u1));

        assertNotNull(response_u1);
        assertEquals(response_u1.getStatus(), Response.ResponseStatus.SUCCESS);

        Request loginRequest_u2 = new Request(Request.RequestType.REGISTER, gson.toJson(user2));
        Response response_u2 = clientHelper2.sendRequest(loginRequest_u2, Response.class);
        System.out.println(gson.toJson(response_u2));
        assertNotNull(response_u2);

        Request loginRequest_u3 = new Request(Request.RequestType.REGISTER, gson.toJson(user3));
        Response response_u3 = clientHelper3.sendRequest(loginRequest_u3, Response.class);
        System.out.println(gson.toJson(response_u3));
        assertNotNull(response_u3);

        Request loginRequest_u4 = new Request(Request.RequestType.REGISTER, gson.toJson(user4));
        Response response_u4 = clientHelper4.sendRequest(loginRequest_u4, Response.class);
        System.out.println(gson.toJson(response_u4));
        assertNotNull(response_u4);
    }

    /*
     * Test 5. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with empty values.
     */
    @Test
    public void test5__() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse response = clientHelper1.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(response.getClass(), PairingResponse.class);
    }

    /*
     * Test 6. Send a UPDATE_PAIRING request with user2. It should return a FAILURE response since user2 has not logged in.
     */
    @Test
    public void test6__() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "user2.getUsername()");
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.ResponseStatus.FAILURE);

        Request loginRequest_u2 = new Request(Request.RequestType.LOGIN, gson.toJson(user2));
        Response response_u2 = clientHelper2.sendRequest(loginRequest_u2, Response.class);
        System.out.println(gson.toJson(response_u2));

        assertNotNull(response_u2);
        assertEquals(response_u2.getStatus(), Response.ResponseStatus.SUCCESS);
    }

    /*
     * Test 7. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with one available user (i.e., user2).
     */
    @Test
    public void test7__() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "user1.getUsername()");
        PairingResponse response = clientHelper1.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(1, response.getAvailableUsers().size());

        Request loginRequest_u3 = new Request(Request.RequestType.LOGIN, gson.toJson(user3));
        Response response_u3 = clientHelper3.sendRequest(loginRequest_u3, Response.class);
        System.out.println(gson.toJson(response_u3));

        assertNotNull(response_u3);
        assertEquals(response_u3.getStatus(), Response.ResponseStatus.SUCCESS);

        Request loginRequest_u4 = new Request(Request.RequestType.LOGIN, gson.toJson(user4));
        Response response_u4 = clientHelper4.sendRequest(loginRequest_u4, Response.class);
        System.out.println(gson.toJson(response_u4));

        assertNotNull(response_u4);
        assertEquals(response_u4.getStatus(), Response.ResponseStatus.SUCCESS);
    }

    /*
     * Test 8. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with three available users (i.e., user1, user3, and user4).
     */
    @Test
    public void test8__() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(3, response.getAvailableUsers().size());
    }

    /*
     * Test 9. Logout user4 by closing the socket connection using the close() function. Send another UPDATE_PAIRING request with user2. It should now
     * return PairingResponse with two available users (i.e., user1 and user3). Because user4 is now offline.
     */
    @Test
    public void test9__() {
        clientHelper4.close();

        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(2, response.getAvailableUsers().size());
    }

    /*
     * Test 10. Login user4 back by sending a LOGIN request. Send another UPDATE_PAIRING request with user2. It should return PairingResponse with three
     * available users (i.e., user1, user3, and, user 4). Because user4 is now back online.
     */
    @Test
    public void test10() {
        clientHelper4 = new SocketClientHelper();

        Request loginRequest = new Request(Request.RequestType.LOGIN, gson.toJson(user4));
        Response response_u4 = clientHelper4.sendRequest(loginRequest, Response.class);
        System.out.println(gson.toJson(response_u4));

        assertNotNull(response_u4);
        assertEquals(response_u4.getStatus(), Response.ResponseStatus.SUCCESS);

        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);

        assertNotNull(response);
        assertEquals(3, response.getAvailableUsers().size());
    }

    /*
     * Test 11. Send a SEND_INVITATION from user1 to user2. It should return a SUCCESS response.
     */
    @Test
    public void test11() {
        Request sendInvitationRequest = new Request(Request.RequestType.SEND_INVITATION, gson.toJson(user2));
        Response response = clientHelper1.sendRequest(sendInvitationRequest, Response.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(Response.ResponseStatus.SUCCESS, response.getStatus());
    }

    /*
     * Test 12. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with an invitation from user1.
     */
    @Test
    public void test12() {
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper2.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertNotNull(pairingResponse.getInvitation());
        currentEventId = pairingResponse.getInvitation().getEventId();
        System.out.println(Integer.toString(currentEventId));
    }

    /*
     * Test 13. Send a DECLINE_INVITATION with user2 of the invitation above. It should return a SUCCESS response.
     */
    @Test
    public void test13() {
        // Test 13
        // Send a DECLINE_INVITATION with user2 of the invitation above.
        // It should return a SUCCESS response.
        System.out.println(Integer.toString(currentEventId));
        Request request = new Request(Request.RequestType.DECLINE_INVITATION, Integer.toString(currentEventId));
        Response response = clientHelper2.sendRequest(request, Response.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(Response.ResponseStatus.SUCCESS, response.getStatus());
    }

    /*
     * Test 14. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with a decline invitation response from user2
     */
    @Test
    public void test14() {
        // Test 14
        // Send a UPDATE_PAIRING request with user1.
        // It should return PairingResponse with a decline invitation response from user2.
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper1.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Event.EventStatus.DECLINED, pairingResponse.getInvitationResponse().getStatus());
    }

    /*
     * Test 15. Send an ACKNOWLEDGE_INVITATION request with user1. It should return a SUCCESS response.
     */
    @Test
    public void test15() {
        // Test 15
        // Send an ACKNOWLEDGE_INVITATION request with user1.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ACKNOWLEDGE_RESPONSE, Integer.toString(currentEventId));
        Response response = clientHelper1.sendRequest(request, Response.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(Response.ResponseStatus.SUCCESS, response.getStatus());
    }

    /*
     * Test 16. Send a SEND_INVITATION from user1 to user3. It should return a SUCCESS response
     */
    @Test
    public void test16() {
        // Test 16
        // Send a SEND_INVITATION from user1 to user3.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.SEND_INVITATION, gson.toJson(user3));
        Response pairingResponse = clientHelper1.sendRequest(request, Response.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Response.ResponseStatus.SUCCESS, pairingResponse.getStatus());
    }

    /*
     * Test 17. Send a UPDATE_PAIRING request with user3. It should return PairingResponse with an invitation from user1.
     */
    @Test
    public void test17() {
        // Test 17
        // Send a UPDATE_PAIRING request with user3.
        // It should return PairingResponse with an invitation from user1.
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper3.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertNotNull(pairingResponse.getInvitation());
        currentEventId = pairingResponse.getInvitation().getEventId();
    }

    /*
     * Test 18. Send an ACCEPTED_INVITATION with user3 of the invitation above. It should return a SUCCESS response.
     */
    @Test
    public void test18() {
        // Test 18
        // Send an ACCEPTED_INVITATION with user3 of the invitation above.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ACCEPT_INVITATION, Integer.toString(currentEventId));
        Response pairingResponse = clientHelper3.sendRequest(request, Response.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Response.ResponseStatus.SUCCESS, pairingResponse.getStatus());
    }

    /*
     * Test 19. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with an accept invitation response from user3.
     */
    @Test
    public void test19() {
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper1.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertNotNull(pairingResponse.getInvitationResponse());
    }

    /*
     * Test 20. Send an ACKNOWLEDGE_INVITATION request with user1. It should return a SUCCESS response
     */
    @Test
    public void test20() {
        // Test 20
        // Send an ACKNOWLEDGE_INVITATION request with user1.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ACKNOWLEDGE_RESPONSE, Integer.toString(currentEventId));
        Response pairingResponse = clientHelper1.sendRequest(request, Response.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Response.ResponseStatus.SUCCESS, pairingResponse.getStatus());
    }

    /*
     * Test 21. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with one available user (i.e., user4).
     * Since user1 and user3 are currently playing a game.
     */
    @Test
    public void test21() {
        // Test 21
        // Send a UPDATE_PAIRING request with user2.
        // It should return PairingResponse with one available user (user4).
        // Since user1 and user3 are currently playing a game.
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper2.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertNotNull(pairingResponse.getAvailableUsers());
        assertEquals(1, pairingResponse.getAvailableUsers().size());
    }

    /*
     * Test 22. Send an ABORT_GAME request with user1. It should return a SUCCESS response.
     */
    @Test
    public void test22() {
        // Test 22
        // Send an ABORT_GAME request with user1.
        // It should return a SUCCESS response.
        System.out.println(currentEventId);
        Request request = new Request(Request.RequestType.ABORT_GAME, Integer.toString(currentEventId));
        Response pairingResponse = clientHelper1.sendRequest(request, Response.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Response.ResponseStatus.SUCCESS, pairingResponse.getStatus());
    }

    /*
     * Test 23. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with three available users (i.e., user1, user3, and user4). Since user1 and user3 game is aborted.
     */
    @Test
    public void test23() {
        // Send a UPDATE_PAIRING request with user2.
        // It should return PairingResponse with three available users (user1, user3, and user4).
        // Since user1 and user3 game is aborted.
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper2.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(3, pairingResponse.getAvailableUsers().size());
    }
}