package socket;

import model.Event;
import model.User;

import java.util.ArrayList;

public class PairingResponse extends Response {
    private ArrayList<User> m_availableUsers;
    private Event m_invitation;
    private Event m_invitationResponse;

    PairingResponse() {
        super(null, null);
        m_availableUsers = null;
        m_invitation = null;
        m_invitationResponse = null;
    }

    PairingResponse(ResponseStatus status, String message, ArrayList<User> availableUsers, Event invitation, Event invitationResponse) {
        super(status, message);
        m_availableUsers = availableUsers;
        m_invitation = invitation;
        m_invitationResponse = invitationResponse;
    }


    public ArrayList<User> getAvailableUsers() {
        return m_availableUsers;
    }

    public void setAvailableUsers(ArrayList<User> availableUsers) {
        m_availableUsers = availableUsers;
    }

    public Event getInvitation() {
        return m_invitation;
    }

    public void setInvitation(Event invitation) {
        m_invitation = invitation;
    }

    public Event getInvitationResponse() {
        return m_invitationResponse;
    }

    public void setInvitationResponse(Event invitationResponse) {
        m_invitationResponse = invitationResponse;
    }
}
