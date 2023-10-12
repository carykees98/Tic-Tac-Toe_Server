package socket;

public class Request {
    private final String m_Data;
    private RequestType m_Type;

    Request() {
        m_Type = null;
        m_Data = "Uninitialized";
    }

    Request(RequestType type, String data) {
        m_Type = type;
        m_Data = data;
    }

    public RequestType getType() {
        return m_Type;
    }

    public void setType(RequestType type) {
        m_Type = type;
    }

    public String getData() {
        return m_Data;
    }

    public enum RequestType {
        LOGIN,
        REGISTER,
        UPDATE_PAIRING,
        SEND_INVITATION,
        ACCEPT_INVITATION,
        ACKNOWLEDGE_RESPONSE,
        REQUEST_MOVE,
        SEND_MOVE,
        ABORT_GAME,
        COMPLETE_GAME
    }
}
