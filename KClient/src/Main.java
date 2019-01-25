import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

/**
 * Created by Kevin Scholten on Jan, 2019
 */


public class Main {
    final String SERVER_IP_ADDRESS = "127.0.0.1";
    final int SERVER_PORT = 1357;
    DataProvider dataProvider = DataProvider.getInstance();
    boolean connected;
    boolean running = true;
    Socket socket;
    Scanner in;
    static PrintWriter writer;
    static BufferedReader reader;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        new Main().run();
    }

    private void run() throws IOException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException, NoSuchPaddingException {
        try {
            socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
            InputStream is = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            java.io.OutputStream os = socket.getOutputStream();
            writer = new PrintWriter(os);
            in = new Scanner(System.in);
            while (!connected) {
                String line = reader.readLine();
                if (line.equals("WELCOME")) sendWelcome();
            }
            while (running) {
                String userInput = in.nextLine();
                String[] cutted = new String[1];
                if (userInput.contains(" ")) cutted = userInput.split(" ");
                else cutted[0] = userInput;
                String command = cutted[0];
                String message = userInput.replace(command + " ", "");
                switch (command.toUpperCase()) {
                    case "/HELP":
                        printHelp();
                        break;
                    case "/LIST":
                        printOnlineList();
                        break;
                    case "/QUIT":
                        quitServer();
                        break;
                    case "/GROUP":
                        sendGroupCommand(message);
                        break;
                    case "/GROUPS":
                        sendMessageToServer("GRPS");
                        break;
                    case "/MSG":
                        sendPrivateMessage("PMSG " + message);
                        break;
                    default:
                        sendBroadcast(userInput);
                        break;
                }
            }
        }catch (ConnectException ce) {
            System.err.println("Can't connect to the server..");
        }
        System.out.println("QUIT CHAT CLIENT. RESTART CLIENT TO RECONNECT...");
    }

    private void sendPrivateMessage(String s) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {

        sendMessageToServer(s);
    }

    private void sendGroupCommand(String message) {
        sendMessageToServer("GRPP "+message);
    }

    private void quitServer() {
        sendMessageToServer("QUIT");
        running = false;
    }

    public static void sendMessageToServer(String msg) {
        writer.println(msg);
        writer.flush();
    }

    private void sendBroadcast(String userInput) {
        if (!userInput.isEmpty())
        sendMessageToServer("BRCST "+userInput);
    }

    private void printOnlineList() {
        writer.println("OLIST");
        writer.flush();
    }

    private void printHelp() {
        System.out.println(Strings.HELP);
    }

    private void sendWelcome() throws IOException, NoSuchAlgorithmException {
        while (true) {
            System.out.print("Please enter your username: ");
            String username = in.nextLine();
            if (username.contains(" ")) {
                System.out.println("You cannot use spaces in your name.. Try again");
            } else if (username.length() > 18) {
                System.out.println("Your username can only be 18 characters or less.. Try again");
            } else {
                writer.println("USRNM "+username);
                writer.flush();
                String serverResponse = reader.readLine();
                if (serverResponse.equals("+OK USRNM")) {
                    System.out.println("Succesfully connected! Use /help for more commands!");
                    connected = true;
                    Thread serverMessageThread = new Thread(new ServerMessage());
                    serverMessageThread.start();
                    receiveKeyPair();
                    break;
                } else if (serverResponse.equals("-ERR USRNM")) {
                    System.out.println("Username already exists, please try again.");
                }
            }
        }
    }

    private void receiveKeyPair() throws NoSuchAlgorithmException {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = gen.generateKeyPair();
            dataProvider.PUBLIC_KEY = keyPair.getPublic();
            dataProvider.PRIVATE_KEY = keyPair.getPrivate();
            sendMessageToServer("PUBKEY "+ Arrays.toString(Base64.getEncoder().encode(dataProvider.PUBLIC_KEY.getEncoded())));
    }
}
