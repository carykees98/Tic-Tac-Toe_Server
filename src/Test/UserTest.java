package Test;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {
    // Assuming the User class has the following properties and methods:
    // String username, String password, String displayName, boolean online
    private User testUser = new User("HAL9000", "PeopleGood", "HAL9k", true);

    @Test
    public void testGetUsername() {
        // Test to ensure the getUsername method returns the correct username
        assertEquals("HAL9000", testUser.getUsername());
    }

    @Test
    public void testGetPassword() {
        // Test to ensure the getPassword method returns the correct password
        assertEquals("PeopleGood", testUser.getPassword());
    }

    @Test
    public void testSetPassword() {
        // Test to ensure the setPassword method correctly sets a new password
        testUser.setPassword("PeopleBad");
        assertEquals("PeopleBad", testUser.getPassword());
    }

    @Test
    public void testGetDisplayName() {
        // Test to ensure the getDisplayName method returns the correct display name
        assertEquals("HAL9k", testUser.getDisplayName());
    }

    @Test
    public void testSetDisplayName() {
        // Test to ensure the setDisplayName method correctly sets a new display name
        testUser.setDisplayName("HAL9Grand");
        assertEquals("HAL9Grand", testUser.getDisplayName());
    }

    @Test
    public void testIsOnline() {
        // Test to ensure the isOnline method correctly reports the user as online
        assertTrue(testUser.isOnline());
    }

    @Test
    public void testSetOnlineStatus() {
        // Test to ensure the setOnlineStatus method correctly updates the online status
        testUser.setOnlineStatus(false);
        assertFalse(testUser.isOnline());
    }

    @Test
    public void testEquals() {
        // Test to ensure the equals method correctly compares two User objects
        User anotherUser = new User("HAL9000", "PeopleGood", "HAL9k", true);
        assertTrue(testUser.equals(anotherUser));
    }
}
