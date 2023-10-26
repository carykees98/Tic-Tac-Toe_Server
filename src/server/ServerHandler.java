package server;

import java.net.Socket;

/**
 * Class that handles communication with the SQLite db
 */
public class ServerHandler extends Thread {
    private Socket m_Socket;
    private String m_Username;

    /**
     * @param socket socket connection to the client
     * @param username username of connected client
     */
    public ServerHandler(Socket socket, String username) {
    }

    /**
     * Run creates a handle to the SQLite db
     */
    @Override
    public void run() {
    }

    /**
     *  close() terminates handle to the SQLite db
     */
    public void close() {
    }
}
