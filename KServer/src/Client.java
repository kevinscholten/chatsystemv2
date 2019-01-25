import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.io.*;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Created by Kevin Scholten on Jan, 2019
 */


public class Client implements Runnable {
    Socket socket;
    String username;
    InputStream inputStream;
    OutputStream outputStream;
    PrintWriter writer;
    PublicKey publicKey;
    IdleThread idleThread;
    Thread idler;
    boolean running = true;
    Group currentGroup;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(outputStream);

        writer.println("WELCOME");
        writer.flush();

        while (true) {
            try {
                String line = reader.readLine();
                String arr[] = new String[1];
                if (line.contains(" ")) {
                    arr = line.split(" ");
                } else {
                    arr[0] = line;
                }
                
                String protocol = arr[0];
                String response = line.replace(arr[0]+" ", "");
                String print = ">> "+getUsername()+": (PROTOCOL) "+protocol;
                if (line.contains(" ")) {
                    print+=" (MESSAGE) "+response;
                }
                if (!protocol.equals("BRCST") && !protocol.equals("PONG"))
                    Server.printToServerConsole(print);
                switch (protocol) {
                    case "USRNM":checkUsername(response);break;
                    case "OLIST":Server.sendOnlineUsers(this);break;
                    case "BRCST":Server.publicMessage(this, response);break;
                    case "GRPP": groupSystem(response);break;
                    case "GRPS": Server.sendGroupList(this);break;
                    case "PMSG": Server.sendPrivateMessage(this, response);break;
                    case "PUBKEY": savePublicKey(response);break;
//                    case "GETPKY": sendPublicKey(response);break;
//                    case "QUIT":Server.disconnectClient(this);break;
                    case "PONG":idleThread.setPongReceived(true);break;
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    private void savePublicKey(String response) throws InvalidKeySpecException, NoSuchAlgorithmException {

        byte[] publicKeyEncoded = Base64.getDecoder().decode(response);
        publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyEncoded));

    }

    private void groupSystem(String response) {
        String[] splitResponse = new String[1];
        if (!response.contains(" ")) splitResponse[0] = response; else splitResponse = response.split(" ");
        if (splitResponse.length>0) {
            switch (splitResponse[0].toLowerCase()) {
                case "create":
                    if (currentGroup==null && splitResponse.length>1) {
                        Server.createGroup(this, splitResponse[1]);
                        sendMessage("Notice: You have succesfully created the group '" + splitResponse[1] + "'");
                    }
                    else if (currentGroup==null)
                        sendError(9);
                    else sendError(8);break;
                case "leave":
                    if (currentGroup!=null) {
                        if (currentGroup.owner!=this) {
                            currentGroup.kickMember(this);
                            currentGroup = null;
                            sendMessage("Notice: You have left your group.");
                        } else
                            sendError(13);
                    } else sendError(4);break;
                case "disband":
                    if (currentGroup!=null) {
                        if (currentGroup.owner==this) {
                            Server.sendGroupMessage(currentGroup, "Notice: The owner of your group has disbanded your group. You have automatically left.");
                            Server.removeGroup(currentGroup);
                            Server.disbandGroup(currentGroup);
                        } else {
                            sendError(11);
                        }
                    } else sendError(4);break;
                case "kick":
                    if (splitResponse.length > 1) {
                        if (currentGroup!=null) {
                            if (currentGroup.owner==this) {
                                if (Server.getClientByName(splitResponse[1])!=null && Server.isPartOfGroup(Server.getClientByName(splitResponse[1]), currentGroup)) {
                                    if (Server.getClientByName(splitResponse[1])!=currentGroup.owner) {
                                        currentGroup.kickMember(Server.getClientByName(splitResponse[1]));
                                        Server.getClientByName(splitResponse[1]).currentGroup=null;
                                        Server.sendGroupMessage(currentGroup, "Notice: "+splitResponse[1]+" has been kicked from your group.");
                                    } else sendError(18);
                                } else sendError(2);
                            } else sendError(11);
                        } else  sendError(4);
                    } else sendError(14);break;
                case "msg":
                    if (splitResponse.length > 1) {
                        if (currentGroup!=null) {
                            String message = response.replace(splitResponse[0]+" ", "");
                            Server.sendGroupMessage(currentGroup, "(GROUP MESSAGE) <"+currentGroup.groupName+"> "+"["+getUsername()+"]: "+message);
                        } else sendError(4);
                    } else sendError(10);break;
                case "invite":
                    if (currentGroup!=null) {
                        if (splitResponse.length > 1){
                            if (Server.doesUsernameExists(splitResponse[1])) {
                                if (currentGroup.owner==this) {
                                    if (Server.getClientByName(splitResponse[1])!=this) {
                                        currentGroup.invite(Server.getClientByName(splitResponse[1]));
                                        sendMessage("Notice: You have succesfully invited "+Server.getClientByName(splitResponse[1]).getUsername());
                                        Server.sendMessageTo(Server.getClientByName(splitResponse[1]), "Notice: You have been invited to '"+currentGroup.groupName+"' by "+ getUsername());
                                    } else sendError(17);
                                } else sendError(11);
                            } else sendError(2);
                        } else sendError(6);
                    } else sendError(4);break;
                case "join":
                    if (currentGroup==null) {
                        if (splitResponse.length > 1) {
                            if (Server.getGroupByName(splitResponse[1])!=null) {
                                if (Server.isInvitedToGroup(this, Server.getGroupByName(splitResponse[1]))){
                                    Server.getGroupByName(splitResponse[1]).addMember(this);
                                    Server.getGroupByName(splitResponse[1]).removeInvite(this);
                                    currentGroup = Server.getGroupByName(splitResponse[1]);
                                    sendMessage("Notice: You have succesfully joined the group '"+splitResponse[1]+"'!");
                                } else sendError(7);
                            } else sendError(16);
                        } else sendError(15);
                    } else sendError(8);break;
                case "info":
                    if (currentGroup!=null) {
                        Server.sendGroupInfo(this, currentGroup);
                    } else sendError(4);break;
                case "help":
                    sendMessage("Available commands: /group create <group name>, /group invite <name>, /group join <group name>, /group leave, /group info, /group help, /group kick <name>, /group msg <group message>, /group disband, /groups");
                    break;
                default:sendError(3);break;
            }
        } else {
            sendError(3);
        }
    }

    private void sendError(int code) {
        switch (code) {
            case 1:
                sendMessage("Error: Enter a username and a message to send MSG. '/msg <user> <message>'.");
                break;
            case 2:
                sendMessage("Error: The user specified is not found.");
                break;
            case 3:
                sendMessage("Error: Please use '/group help' for group commands.");
                break;
            case 4:
                sendMessage("Error: You are not part of a group.");
                break;
            case 5:
                sendMessage("Error: Only group owner can invite people.");
                break;
            case 6:
                sendMessage("Error: Use '/group invite <name>' to invite users.");
                break;
            case 7:
                sendMessage("Error: You are not invited for this group.");
                break;
            case 8:
                sendMessage("Error: You are already a member of a group.");
                break;
            case 9:
                sendMessage("Error: Please use '/group create <name>'");
                break;
            case 10:
                sendMessage("Error: Please use '/group msg <message>' to send messages to your group.");
                break;
            case 11:
                sendMessage("Error: Only the group owner can do this.");
                break;
            case 12:
                sendMessage("Error: This user is already member of your group!");
                break;
            case 13:
                sendMessage("Error: You cannot leave your own group. Use '/group disband' to delete your group.");
                break;
            case 14:
                sendMessage("Error: Please use '/group kick <name>' to kick someone.");
                break;
            case 15:
                sendMessage("Error: Please use '/group join <group name>' to join a group.");
                break;
            case 16:
                sendMessage("Error: The group specified is not found.");
                break;
            case 17:
                sendMessage("Error: You cannot invite yourself!");
                break;
            case 18:
                sendMessage("Error: You cannot kick yourself!");
                break;
        }
    }

    private void checkUsername(String response) throws InterruptedException {
        if (Server.doesUsernameExists(response)) {
            writer.println("-ERR USRNM");
        } else {
            writer.println("+OK USRNM");
            username = response;
            Server.printToServerConsole("[+] "+username+" connected.");
            Server.sendMessageToAllClients("[+] "+username+" has joined the server.");
//            idler = new Thread(new IdleThread(this));
//            idler.start();
        }
        writer.flush();
    }

    public void sendMessage(String msg) {
        writer.println(msg);
        writer.flush();
    }

    public void setCurrentGroup(Group currentGroup) {
        this.currentGroup = currentGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
