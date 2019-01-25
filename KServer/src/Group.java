import java.util.ArrayList;

/**
 * Created by Kevin Scholten on Dec, 2018
 */


public class Group {
    String groupName;
    ArrayList<Client> members;
    ArrayList<Client> invited;
    Client owner;

    public Group(String name, Client owner) {
        this.groupName = name;
        this.owner = owner;
        members = new ArrayList<>();
        invited = new ArrayList<>();
        members.add(owner);
    }

    public void addMember(Client c) {
        members.add(c);
    }

    public void kickMember(Client c) {
        members.remove(c);
    }

    public void invite(Client c) {
        invited.add(c);
    }

    public boolean isInvited(Client c) {
        for (Client Client : invited) {
            if (Client ==c) return true;
        }
        return false;
    }
    public boolean isMember(Client c) {
        for (Client Client : members) {
            if (Client ==c) return true;
        }
        return false;
    }

    public void removeInvite(Client cli) {
        invited.remove(cli);
    }
}
