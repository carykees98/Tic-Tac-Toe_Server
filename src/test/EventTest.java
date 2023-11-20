package test;

import model.Event;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventTest {
    final Event event = new Event(1, "Sender", "Opponent", Event.EventStatus.PENDING, "Turn", 42);

    @Test
    public void getLastMove() {
        assertEquals(42, event.getMove());
    }

    @Test
    public void setLastMove() {
        event.setMove(57);
        assertEquals(57, event.getMove());
    }

    @Test
    public void getTurn() {
        assertEquals("Turn", event.getTurn());
    }

    @Test
    public void setTurn() {
        event.setTurn("Player2");
        // Ensure that getTurn() returns the value we set
        assertEquals("Player2", event.getTurn());
    }

    @Test
    public void getOpponent() {
        assertEquals("Opponent", event.getOpponent());
    }

    @Test
    public void getSender() {
        assertEquals("Sender", event.getSender());
    }

    @Test
    public void getEventID() {
        assertEquals(1, event.getEventId());
    }

    @Test
    public void getStatus() {
        Event event1 = new Event(6, "Sender", "Opponent", Event.EventStatus.DECLINED, "Player1", 30);
        // Ensure that getStatus() returns the correct status
        assertEquals(Event.EventStatus.DECLINED, event1.getStatus());
    }


    @Test
    public void setEventStatus() {
        event.setStatus(Event.EventStatus.ACCEPTED);
        assertEquals(Event.EventStatus.ACCEPTED, event.getStatus());
    }

    @Test
    public void equals() {
        // Create two Event objects with the same eventID
        Event event1 = new Event(7, "Sender", "Opponent", Event.EventStatus.PENDING, "Player1", 35);
        Event event2 = new Event(7, "OtherSender", "OtherOpponent", Event.EventStatus.PENDING, "Player2", 35);
        // Ensure that the equals method correctly identifies them as equal
        assertTrue(event1.equals(event2));
    }
}
