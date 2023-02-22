package plugin.Function;

import java.util.Date;
import java.text.SimpleDateFormat;

public class User
{
    private final String userID;
    private final String modelType;
    private final boolean update;
    private final Date date;

    public User(String userID, String modelType, boolean update)
    {
        this.userID = userID;
        this.modelType = modelType;
        this.update = update;
        this.date = new Date();
    }

    public String getUserID()
    {
        return userID;
    }

    public String getModelType()
    {
        return modelType;
    }

    public boolean getUpdate()
    {
        return update;
    }

    public String getDate()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return formatter.format(this.date);
    }
}
