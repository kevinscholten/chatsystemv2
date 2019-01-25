/**
 * Created by Kevin Scholten on Jan, 2019
 */


public class Strings {

    public static String getErrorString(int code) {
        String errorMessage="-ERR Error: ";
        switch (code) {
            case 1:
                errorMessage+="Enter a username and a message to send MSG. '/msg <user> <message>'.";
                break;
            case 2:
                errorMessage+="The user specified is not found.";
                break;
            case 3:
                errorMessage+="Please use '/group help' for group commands.";
                break;
            case 4:
                errorMessage+="You are not part of a group.";
                break;
            case 5:
                errorMessage+="Only group owner can invite people.";
                break;
            case 6:
                errorMessage+="Use '/group invite <name>' to invite users.";
                break;
            case 7:
                errorMessage+="You are not invited for this group.";
                break;
            case 8:
                errorMessage+="You are already a member of a group.";
                break;
            case 9:
                errorMessage+="Please use '/group create <name>'";
                break;
            case 10:
                errorMessage+="Please use '/group msg <message>' to send messages to your group.";
                break;
            case 11:
                errorMessage+="Only the group owner can do this.";
                break;
            case 12:
                errorMessage+="This user is already member of your group!";
                break;
            case 13:
                errorMessage+="You cannot leave your own group. Use '/group disband' to delete your group.";
                break;
            case 14:
                errorMessage+="Please use '/group kick <name>' to kick someone.";
                break;
            case 15:
                errorMessage+="Please use '/group join <group name>' to join a group.";
                break;
            case 16:
                errorMessage+="The group specified is not found.";
                break;
            case 17:
                errorMessage+="You cannot invite yourself!";
                break;
            case 18:
                errorMessage+="You cannot kick yourself!";
                break;
            case 19:
                errorMessage+="A group with that group name already exists. Try another name.";
                break;
            case 20:
                errorMessage+="You cannot send yourself a private message!";
                break;
        }
        return errorMessage;
    }
}
