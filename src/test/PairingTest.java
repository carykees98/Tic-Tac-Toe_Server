package test;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import model.User;
import java.sql.SQLException;
import java.lang.*;
import server.DatabaseHelper;
import server.SocketServer;
import socket.Request;
import socket.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PairingTest {

    private User user1, user2, user3, user4;
    private SocketClientHelper clientHelper1, clientHelper2, clientHelper3, clientHelper4;

    Thread mainThread = new Thread(() -> {
        try {
            DatabaseHelper.getInstance().truncateTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        SocketServer.main(null);
    });
    mainThread.start();
    Thread.sleep(1000);

    Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    @BeforeClass
    public void setUp() {
        // Initialize User objects with unique usernames and display names
        user1 = new User("username1", "passwdName1", "u1", true);
        user2 = new User("username2", "passwdName2", "u2", true);
        user3 = new User("username3", "passwdName3", "u3", true);
        user4 = new User("username4", "passwdName4", "u4", true);

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
        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, user1.getUsername());
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
        Request registerRequest_u1 = new Request(Request.RequestType.REGISTER, user1.getUsername());
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

        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, tmp_user1.getUsername());
        Response response_u1 = clientHelper1.sendRequest(loginRequest_u1, Response.class);
        System.out.println(gson.toJson(response_u1));

        assertNotNull(response_u1);
        assertEquals(response_u1.getStatus(), Response.ResponseStatus.FAILURE);
    }

    /*
     * Test 4. Send another LOGIN request with user1, but this time with the correct password. It should return a SUCCESS response.
     */
    @Test
    public void testLoginClient1AfterRegisteredWithRightPassword() {
        Request loginRequest_u1 = new Request(Request.RequestType.LOGIN, user1.getUsername());
        Response response_u1 = clientHelper1.sendRequest(loginRequest_u1, Response.class);
        System.out.println(gson.toJson(response_u1));

        assertNotNull(response_u1);
        assertEquals(response_u1.getStatus(), Response.ResponseStatus.SUCCESS);
    }

    /*
     * Test 4.1. Now that user registration is working, register the other users by sending a REGISTER request with user2, user3, and user4.
     */
    @Test
    public void testRegisterClient2to4() {
        Request loginRequest_u2 = new Request(Request.RequestType.REGISTER, user1.getUsername());
        Response response_u2 = clientHelper2.sendRequest(loginRequest_u2, Response.class);
        System.out.println(gson.toJson(response_u2));
        assertNotNull(response_u2);

        Request loginRequest_u3 = new Request(Request.RequestType.REGISTER, user3.getUsername());
        Response response_u3 = clientHelper1.sendRequest(loginRequest_u3, Response.class);
        System.out.println(gson.toJson(response_u3));
        assertNotNull(response_u3);

        Request loginRequest_u4 = new Request(Request.RequestType.REGISTER, user4.getUsername());
        Response response_u4 = clientHelper4.sendRequest(loginRequest_u4, Response.class);
        System.out.println(gson.toJson(response_u4));
        assertNotNull(response_u4);
    }

    /*
     * Test 5. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with empty values.
     */
    @Test
    public void testUpdatePairingWithUser1_EmptyResponse() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user1.getUsername());
        Response response = clientHelper1.sendRequest(updatePairingRequest, Response.class);

        assertNotNull(response);
    }

    /*
     * Test 6. Send a UPDATE_PAIRING request with user2. It should return a FAILURE response since user2 has not logged in.
     */
    @Test
    public void testUpdatePairingWithUser2_FailureResponse() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        Response response = clientHelper2.sendRequest(updatePairingRequest, Response.class);

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.ResponseStatus.FAILURE);
    }

    /*
     * Test 7. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with one available user (i.e., user2).
     */
    @Test
    public void testUpdatePairingWithUser1_OneAvailableUser() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user1.getUsername());
        Response response = clientHelper1.sendRequest(updatePairingRequest, Response.class);

        assertNotNull(response);
        // TODO not sure what PairingResponse is
    }

    /*
     * Test 7.1. Login the rest of the users by sending a LOGIN request with user3 and user4.
     */
    @Test
    public void testLoginUser3AndUser4() {
        Request loginRequest_u3 = new Request(Request.RequestType.LOGIN, user3.getUsername());
        Response response_u3 = clientHelper3.sendRequest(loginRequest_u3, Response.class);
        System.out.println(gson.toJson(response_u3));

        assertNotNull(response_u3);
        assertEquals(response_u3.getStatus(), Response.ResponseStatus.SUCCESS);

        Request loginRequest_u4 = new Request(Request.RequestType.LOGIN, user4.getUsername());
        Response response_u4 = clientHelper3.sendRequest(loginRequest_u4, Response.class);
        System.out.println(gson.toJson(response_u4));

        assertNotNull(response_u4);
        assertEquals(response_u4.getStatus(), Response.ResponseStatus.SUCCESS);
    }

    /*
     * Test 8. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with three available users (i.e., user1, user3, and user4).
     */
    @Test
    public void testUpdatePairingWithUser2_ThreeAvailableUsers() {
        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        Response response = clientHelper2.sendRequest(updatePairingRequest, Response.class);

        assertNotNull(response);
        // TODO same as 7 not sure
    }

    /*
     * Test 9. Logout user4 by closing the socket connection using the close() function. Send another UPDATE_PAIRING request with user2. It should now
     * return PairingResponse with two available users (i.e., user1 and user3). Because user4 is now offline.
     */
    @Test
    public void testUpdatePairingPostUser4Logout() {
        clientHelper4.close();

        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        Response response = clientHelper2.sendRequest(updatePairingRequest, Response.class);

        assertNotNull(response);
        System.out.println(response.getMessage());
        // TODO
    }

    /*
     * Test 10. Login user4 back by sending a LOGIN request. Send another UPDATE_PAIRING request with user2. It should return PairingResponse with three
     * available users (i.e., user1, user3, and, user 4). Because user4 is now back online.
     */
    @Test
    public void testLoginUser4AndUpdatePairing() {
        Request loginRequest_u4 = new Request(Request.RequestType.LOGIN, user4.getUsername());
        Response response_u4 = clientHelper3.sendRequest(loginRequest_u4, Response.class);
        System.out.println(gson.toJson(response_u4));

        assertNotNull(response_u4);
        assertEquals(response_u4.getStatus(), Response.ResponseStatus.SUCCESS);

        Request updatePairingRequest = new Request(Request.RequestType.UPDATE_PAIRING, user2.getUsername());
        Response response = clientHelper2.sendRequest(updatePairingRequest, Response.class);

        assertNotNull(response);
        // TODO
    }

    /*
     * Test 11. Send a SEND_INVITATION from user1 to user2. It should return a SUCCESS response.
     */
    @Test
    public void testSendInvitationFromUser1ToUser2() {
        Request sendInvitationRequest = new Request(Request.RequestType.SEND_INVITATION, user1.getUsername());
        Response response = clientHelper1.sendRequest(sendInvitationRequest, Response.class);

        assertNotNull(response);
        assertEquals(Response.ResponseStatus.SUCCESS, response.getType());
    }

    /*
     * Test 12. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with an invitation from user1.
     */

    /*
     * Test 13. Send a DECLINE_INVITATION with user2 of the invitation above. It should return a SUCCESS response.
     */

    /*
     * Test 14. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with a decline invitation response from user2
     */

    /*
     * Test 15. Send an ACKNOWLEDGE_INVITATION request with user1. It should return a SUCCESS response.
     */

    /*
     * Test 16. Send a SEND_INVITATION from user1 to user3. It should return a SUCCESS response
     */

    /*
     * Test 17. Send a UPDATE_PAIRING request with user3. It should return PairingResponse with an invitation from user1.
     */

    /*
     * Test 18. Send an ACCEPTED_INVITATION with user3 of the invitation above. It should return a SUCCESS response.
     */

    /*
     * Test 19. Send a UPDATE_PAIRING request with user1. It should return PairingResponse with an accept invitation response from user3.
     */

    /*
     * Test 20. Send an ACKNOWLEDGE_INVITATION request with user1. It should return a SUCCESS response
     */

    /*
     * Test 21. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with one available user (i.e., user4).
     * Since user1 and user3 are currently playing a game.
     */

    /*
     * Test 22. Send an ABORT_GAME request with user1. It should return a SUCCESS response.
     */

    /*
     * Test 23. Send a UPDATE_PAIRING request with user2. It should return PairingResponse with three available users (i.e., user1, user3, and user4). Since user1 and user3 game is aborted.
     */

}