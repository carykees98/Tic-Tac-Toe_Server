package socket;

public class Response {
    public ResponseStatus m_Status;
    public String m_Message;

    Response() {
        m_Status = null;
        m_Message = "Uninitialized";
    }

    Response(ResponseStatus status, String message) {
        m_Status = status;
        m_Message = message;
    }

    public String getMessage() {
        return m_Message;
    }

    public ResponseStatus getStatus() {
        return m_Status;
    }

    public enum ResponseStatus {
        SUCCESS,
        FAILURE
    }
}
