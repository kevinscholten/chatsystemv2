import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.util.Base64;

/**
 * Created by Kevin Scholten on Jan, 2019
 */


public class ServerMessage implements Runnable {
    boolean isRunning = true;
    DataProvider dataProvider = DataProvider.getInstance();

    @Override
    public void run() {
        while (isRunning) {
            try {
                String line = Main.reader.readLine();
                String arr[] = new String[1];
                if (line.contains(" ")) arr = line.split(" "); else  arr[0] = line;
                String protocol = arr[0];
                String response = line.replace(arr[0]+" ", "");
                switch (protocol) {
                    case "OLIST":
                        System.out.println(response);break;
                    case "PING":
                        Main.sendMessageToServer("PONG");break;
                    case "PMSG":
                        decryptMessage(response);break;
                    case "+OK":
                        System.out.println(response);break;
                    case "-ERR":
                        System.out.println(response);break;
                    default:
                        System.out.println(line);break;
                }
            } catch (Exception e) {
                isRunning=false;
            }
        }
        System.err.println("Connection with server lost. Try to restart your client...");
    }

    private void decryptMessage(String response) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        String cutted[] = new String[1];
        if (response.contains(" ")) cutted = response.split(" "); else cutted[0] = response;
        String message = response.replace(cutted[0], "");

        System.out.println("(PRIVATE MSG) "+cutted[0]+": "+message);
    }




}
