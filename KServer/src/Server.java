import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Kevin Scholten on Jan, 2019
 */


public class Server {
    ServerSocket serverSocket;
    static ArrayList<Client> clients = new ArrayList<>();
    static ArrayList<Group> groups = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new Server().run();
    }

    private void run() throws IOException {
        serverSocket = new ServerSocket(1357);
        while (true) {
            Socket socket = serverSocket.accept();
            Client client = new Client(socket);
            clients.add(client);
            Thread clientThread = new Thread(client);
            clientThread.start();
        }
    }

    static boolean doesUsernameExists(String username) {
        for (Client c : clients) {
            if (c.getUsername()!=null && c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static void sendOnlineUsers(Client client) {
        String s = "OLIST Online users: ";
        for (int i = 0; i < clients.size(); i++) {
            if (i==(clients.size()-1)) {
                s+=clients.get(i).getUsername();
            } else
            s+=clients.get(i).getUsername()+", ";
        }
        client.sendMessage(s);
    }

    public static void publicMessage(Client client, String response) {
        for (Client c : clients) {
            if (client.currentGroup==null) {
            c.sendMessage("" + client.getUsername() + ": "+response);
            printToServerConsole("" + client.getUsername() + ": "+response);}
            else { c.sendMessage("["+client.currentGroup.groupName+"] " + client.getUsername() + ": "+response);
            printToServerConsole("["+client.currentGroup.groupName+"] " + client.getUsername() + ": "+response); }
        }
    }

    public static void sendMessageToAllClients(String msg) {
        for (Client c : clients) {
            c.sendMessage(msg);
        }
    }

    public static void disconnectClient(Client client) {
        for (Client c : clients)
            if (c==client) {
                client.running = false;
//                client.idler.interrupt();
                clients.remove(client);
                break;
            }
        printToServerConsole("[-] "+client.getUsername() + " disconnected.");
        sendMessageToAllClients("[-] "+client.getUsername() + " has left the server.");
    }

    public static void printToServerConsole(String msg) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("d-M H:m");
        String dateString = sdf.format(date);
        System.out.println(dateString + " " + msg);
    }

    public static Group createGroup(Client client, String s) {
        Group g = new Group(s, client);
        client.currentGroup=g;
        groups.add(g);
        return g;
    }

    public static void sendGroupMessage(Group group, String msg) {
        for (Client c : group.members) {
            c.sendMessage(msg);
        }
    }

    public static Client getClientByName(String s) {
        for (Client c : clients) {
            if (c.getUsername()!=null && c.getUsername().equals(s)) return c;
        }
        return null;
    }

    public static boolean isPartOfGroup(Client clientByName, Group group) {
        for (Client c : group.members) {
            if (c==clientByName) return true;
        }
        return false;
    }

    public static boolean isInvitedToGroup(Client client, Group group) {
        for (Client c : group.invited) {
            if (c==client) {
                return true;
            }
        }
        return false;
    }

    public static Group getGroupByName(String s) {
        for (Group g : groups) {
            if (g.groupName.equals(s)) return g;
        }return null;
    }

    public static void sendMessageTo(Client clientByName, String s) {
        for (Client c : clients) {
            if (c==clientByName) {
                c.sendMessage(s);
            }
        }
    }

    public static void disbandGroup(Group currentGroup) {
        for (Client c : currentGroup.members) {
            c.currentGroup=null;
        }
        currentGroup.members.clear();
        for (Group g : groups) {
            if (g==currentGroup) groups.remove(g);
            break;
        }
    }

    public static void sendGroupInfo(Client client, Group currentGroup) {
        String s = "CURRENT GROUP: "+currentGroup.groupName.toUpperCase()+" - "+currentGroup.members.size()+" Members - Owner: "+currentGroup.owner.getUsername()+"" +
                " - Member(s): ";
        for (Client c : currentGroup.members) {
            s+=c.getUsername()+", ";
        }
        client.sendMessage(s);
    }

    public static void sendGroupList(Client client) {
        String s = "ALL GROUPS: ";
        for (Group c : groups) {
            s+=c.groupName+" ("+c.members.size()+" members), ";
        }
        client.sendMessage(s);
    }

    public static void removeGroup(Group currentGroup) {
        for (Group g : groups) {
            if (g==currentGroup) {
                groups.remove(g);
                break;
            }
        }
    }

    public static void sendPrivateMessage(Client sender, String response) {
        String[] cutted = new String[1];
        if (response.contains(" ")) cutted = response.split(" "); else cutted[0] = response;
        Client receiver = getClientByName(cutted[0]);
        String message = response.replace(cutted[0], "");

        if (cutted.length > 1) {
            if (doesUsernameExists(receiver.getUsername())) {
                receiver.sendMessage("PMSG "+sender.getUsername()+" "+message);
                sender.sendMessage("PRIVATE MSG to "+receiver.getUsername()+": "+message);
            } else sender.sendMessage("Error: User specified is not found.");
        } else {
            sender.sendMessage("Error: Please use /msg <username> <message>");
        }


    }
}
