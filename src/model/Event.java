package model;

import org.jetbrains.annotations.NotNull;

public class Event {

    private final int m_EventID;
    private final String m_Sender;
    private final String m_Opponent;
    private EventStatus m_Status;
    private String m_Turn;
    private int m_LastMove;

    Event() {
        m_Sender = m_Opponent = m_Turn = "Uninitialized";
        m_EventID = m_LastMove = -1;
        m_Status = null;
    }

    Event(int eventID, String sender, String opponent, EventStatus status, String turn, int lastMove) {
        m_EventID = eventID;
        m_Sender = sender;
        m_Opponent = opponent;
        m_Status = status;
        m_Turn = turn;
        m_LastMove = lastMove;
    }


    public int getLastMove() {
        return m_LastMove;
    }

    public void setLastMove(int lastMove) {
        m_LastMove = lastMove;
    }

    public String getTurn() {
        return m_Turn;
    }

    public void setTurn(String turn) {
        m_Turn = turn;
    }

    public String getOpponent() {
        return m_Opponent;
    }

    public String getSender() {
        return m_Sender;
    }

    public int getEventID() {
        return m_EventID;
    }

    public EventStatus getStatus() {
        return m_Status;
    }

    public void setEventStatus(EventStatus status) {
        m_Status = status;
    }

    public boolean equals(@NotNull Event otherEvent) {
        return m_EventID == otherEvent.m_EventID;
    }

    public enum EventStatus {
        PENDING,
        DECLINED,
        ACCEPTED,
        PLAYING,
        COMPLETED,
        ABORTED
    }

}
