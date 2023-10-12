package model;

import org.jetbrains.annotations.NotNull;

public class User {
    private final String m_Username;
    private String m_Password;
    private String m_DisplayName;
    private boolean m_Online;

    User() {
        m_Username = m_Password = m_DisplayName = "Uninitialized";
        m_Online = false;
    }

    User(String username, String password, String displayName, boolean online) {
        m_Username = username;
        m_Password = password;
        m_DisplayName = displayName;
        m_Online = online;
    }

    public String getUsername() {
        return m_Username;
    }

    public String getPassword() {
        return m_Password;
    }

    public void setPassword(String password) {
        m_Password = password;
    }

    public String getDisplayName() {
        return m_DisplayName;
    }

    public void setDisplayName(String displayName) {
        m_DisplayName = displayName;
    }

    public boolean isOnline() {
        return m_Online;
    }

    public void setOnlineStatus(boolean online) {
        m_Online = online;
    }

    public boolean equals(@NotNull User otherUser) {
        return m_Username.equals(otherUser.m_Username);
    }
}
