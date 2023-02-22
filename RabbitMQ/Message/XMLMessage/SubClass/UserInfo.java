package Message.XMLMessage.SubClass;

import java.io.Serializable;

public class UserInfo implements Serializable
{
    private final String userID;

    // Construct function
    public UserInfo(String userID)
    {
        this.userID = userID;
    }

    // Access Function
    public String getUserID()
    {
        return userID;
    }
}
