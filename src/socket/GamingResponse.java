package socket;

public class GamingResponse extends Response {
    private int m_Move;
    private boolean m_Active;

    GamingResponse() {
        super();
        m_Move = -1;
        m_Active = false;
    }

    GamingResponse(ResponseStatus status, String message, int move, boolean active) {
        super(status, message);
        m_Move = move;
        m_Active = active;
    }

    public int getMove() {
        return m_Move;
    }

    public void setMove(int move) {
        m_Move = move;
    }

    public boolean isActive() {
        return m_Active;
    }

    public void setActive(boolean active) {
        m_Active = active;
    }
}
