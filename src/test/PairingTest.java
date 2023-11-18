package test;

import model.Event;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import model.User;

import java.sql.SQLException;
import java.lang.*;

import server.DatabaseHelper;
import server.SocketServer;
import socket.PairingResponse;
import socket.Request;
import socket.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PairingTest {

    private static User user1;
    private static User user2;
    private static User user3;
    private static User user4;
    private static SocketClientHelper clientHelper1;
    private static SocketClientHelper clientHelper2;
    private static SocketClientHelper clientHelper3;
    private static SocketClientHelper clientHelper4;

    Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

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
        user1 = new User("user1", "passwdName1", null, false);
        user2 = new User("user2", "passwdName2", null, false);
        user3 = new User("user3", "passwdName3", null, false);
        user4 = new User("user4", "passwdName4", null, false);

        // Initialize SocketClientHelper objects for each user
        clientHelper1 = new SocketClientHelper();
        clientHelper2 = new SocketClientHelper();
        clientHelper3 = new SocketClientHelper();
        clientHelper4 = new SocketClientHelper();

//        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, user1.getUsername());
//        Response response_u1 = clientHelper1.sendRequest(loginRequest_u1, Response.class);
//        System.out.println(gson.toJson(response_u1));
//        // Assertions to verify the response
//        assertNotNull(response_u1);
//
//        Request loginRequest_u2 = new Request(Request.RequestType.LOGIN, user1.getUsername());
//        Response response_u2 = clientHelper2.sendRequest(loginRequest_u2, Response.class);
//        System.out.println(gson.toJson(response_u2));
//        // Assertions to verify the response
//        assertNotNull(response_u2);
//
//        Request loginRequest_u3 = new Request(Request.RequestType.LOGIN, user3.getUsername());
//        Response response_u3 = clientHelper1.sendRequest(loginRequest_u3, Response.class);
//        System.out.println(gson.toJson(response_u3));
//        // Assertions to verify the response
//        assertNotNull(response_u3);
//
//        Request loginRequest_u4 = new Request(Request.RequestType.LOGIN, user4.getUsername());
//        Response response_u4 = clientHelper4.sendRequest(loginRequest_u4, Response.class);
//        System.out.println(gson.toJson(response_u4));
//        // Assertions to verify the response
//        assertNotNull(response_u4);
    }

    /*
     * Test 1. Send a LOGIN request with user1. It should return a FAILURE response since the user is not registered.
     */
    @Test
    public void testLoginClient1() {
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
    public void testRegisterClient1() {
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
    public void testLoginClient1AfterRegisteredWithWrongPassword() {
        User tmp_user1 = user1;
        tmp_user1.setPassword("ABCD");

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
    public void testLoginClient() {
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
    public void testUpdatePairingWithUser1_EmptyResponse() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse response = clientHelper1.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
    }

    /*
     * Test 6. Send a UPDATE_PAIRING request with user2. It should return a FAILURE response since user2 has not logged in.
     */
    @Test
    public void testUpdatePairingWithUser2_FailureResponse() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "user2.getUsername()");
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.ResponseStatus.FAILURE);
    }

    /*
     * Test 7. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with one available user (i.e., user2).
     */
    @Test
    public void testUpdatePairingWithUser1_OneAvailableUser() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "user1.getUsername()");
        PairingResponse response = clientHelper1.sendRequest(updatePairingRequest, PairingResponse.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(1, response.getAvailableUsers().size());

        Request loginRequest_u2 = new Request(Request.RequestType.LOGIN, gson.toJson(user2));
        Response response_u2 = clientHelper2.sendRequest(loginRequest_u2, Response.class);
        System.out.println(gson.toJson(response_u2));

        assertNotNull(response_u2);
        assertEquals(response_u2.getStatus(), Response.ResponseStatus.SUCCESS);

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
    public void testUpdatePairingWithUser2_ThreeAvailableUsers() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);

        assertNotNull(response);
        assertEquals(3, response.getAvailableUsers().size());
    }

    /*
     * Test 9. Logout user4 by closing the socket connection using the close() function. Send another UPDATE_PAIRING request with user2. It should now
     * return PairingResponse with two available users (i.e., user1 and user3). Because user4 is now offline.
     */
    @Test
    public void testUpdatePairingPostUser4Logout() {
        clientHelper4.close();

        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);

        assertNotNull(response);
        System.out.println(response.getMessage());
    }

    /*
     * Test 10. Login user4 back by sending a LOGIN request. Send another UPDATE_PAIRING request with user2. It should return PairingResponse with three
     * available users (i.e., user1, user3, and, user 4). Because user4 is now back online.
     */
    @Test
    public void testLoginUser4AndUpdatePairing() {
        Request loginRequest_u4 = new Request(Request.RequestType.LOGIN, gson.toJson(user4));
        Response response_u4 = clientHelper4.sendRequest(loginRequest_u4, Response.class);
        System.out.println(gson.toJson(response_u4));

        assertNotNull(response_u4);
        assertEquals(response_u4.getStatus(), Response.ResponseStatus.SUCCESS);

        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        PairingResponse response = clientHelper2.sendRequest(updatePairingRequest, PairingResponse.class);

        assertNotNull(response);
    }

    /*
     * Test 11. Send a SEND_INVITATION from user1 to user2. It should return a SUCCESS response.
     */
    @Test
    public void testSendInvitationFromUser1ToUser2() {
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
    public void testUpdatePairingWithUser2() {
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        PairingResponse pairingResponse = clientHelper2.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertNotNull(pairingResponse.getInvitation());
    }

    /*
     * Test 13. Send a DECLINE_INVITATION with user2 of the invitation above. It should return a SUCCESS response.
     */
    @Test
    public void testDeclineInvitationWithUser2() {
        // Test 13
        // Send a DECLINE_INVITATION with user2 of the invitation above.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.DECLINE_INVITATION, "");
        Response response = clientHelper2.sendRequest(request, Response.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(Response.ResponseStatus.SUCCESS, response.getStatus());
    }

    /*
     * Test 14. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with a decline invitation response from user2
     */
    @Test
    public void testUpdatePairingWithUser1() {
        // Test 14
        // Send a UPDATE_PAIRING request with user1.
        // It should return PairingResponse with a decline invitation response from user2.
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper1.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(pairingResponse.getInvitationResponse().getStatus(), Event.EventStatus.DECLINED);
    }

    /*
     * Test 15. Send an ACKNOWLEDGE_INVITATION request with user1. It should return a SUCCESS response.
     */
    @Test
    public void testAcknowledgeInvitationWithUser1() {
        // Test 15
        // Send an ACKNOWLEDGE_INVITATION request with user1.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ACKNOWLEDGE_RESPONSE, "");
        Response response = clientHelper1.sendRequest(request, Response.class);
        System.out.println(gson.toJson(response));

        assertNotNull(response);
        assertEquals(Response.ResponseStatus.SUCCESS, response.getStatus());
    }

    /*
     * Test 16. Send a SEND_INVITATION from user1 to user3. It should return a SUCCESS response
     */
    @Test
    public void testSendInvitationFromUser1ToUser3() {
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
    public void testUpdatePairingWithUser3() {
        // Test 17
        // Send a UPDATE_PAIRING request with user3.
        // It should return PairingResponse with an invitation from user1.
        Request request = new Request(Request.RequestType.UPDATE_PAIRING, "");
        PairingResponse pairingResponse = clientHelper3.sendRequest(request, PairingResponse.class);
        System.out.println(gson.toJson(pairingResponse));


        assertNotNull(pairingResponse);
        assertNotNull(pairingResponse.getInvitation());
    }

    /*
     * Test 18. Send an ACCEPTED_INVITATION with user3 of the invitation above. It should return a SUCCESS response.
     */
    @Test
    public void testAcceptedInvitationWithUser3() {
        // Test 18
        // Send an ACCEPTED_INVITATION with user3 of the invitation above.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ACCEPT_INVITATION, "");
        Response pairingResponse = clientHelper3.sendRequest(request, Response.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Response.ResponseStatus.SUCCESS, pairingResponse.getStatus());
    }

    /*
     * Test 19. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with an accept invitation response from user3.
     */
    @Test
    public void testUpdatePairingWithUser1AfterAcceptance() {

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
    public void testAcknowledgeInvitationWithUser1AfterAcceptance() {
        // Test 20
        // Send an ACKNOWLEDGE_INVITATION request with user1.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ACKNOWLEDGE_RESPONSE, "");
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
    public void testUpdatePairingWithUser2AfterGameStart() {
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
    public void testAbortGameWithUser1() {
        // Test 22
        // Send an ABORT_GAME request with user1.
        // It should return a SUCCESS response.
        Request request = new Request(Request.RequestType.ABORT_GAME, "");
        Response pairingResponse = clientHelper1.sendRequest(request, Response.class);
        System.out.println(gson.toJson(pairingResponse));

        assertNotNull(pairingResponse);
        assertEquals(Response.ResponseStatus.SUCCESS, pairingResponse.getStatus());
    }

    /*
     * Test 23. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with three available users (i.e., user1, user3, and user4). Since user1 and user3 game is aborted.
     */
    @Test
    public void testUpdatePairingWithUser2AfterAbortGame() {
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