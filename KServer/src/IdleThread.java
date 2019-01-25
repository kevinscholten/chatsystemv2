/**
 * Created by Kevin Scholten on Dec, 2018
 */


public class IdleThread implements Runnable {
    Client clientHandler;
    boolean pongReceived;
    boolean keepRunning = true;

    public IdleThread(Client clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void setPongReceived(boolean pongReceived) {
        this.pongReceived = pongReceived;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                Thread.sleep(8000);
                pongReceived = false;
                clientHandler.sendMessage("PING");
                Thread.sleep(2000);
                if (!pongReceived) {
                    keepRunning = false;
                    Server.disconnectClient(clientHandler);
                }
            } catch (Exception e) {

            }
        }
    }

    public void setRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}

