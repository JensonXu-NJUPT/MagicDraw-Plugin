package Message;

import java.io.Serializable;
import java.util.Date;

public class BaseMessage implements Serializable
{
    private final Date sendTime;
    private final Object content;

    // Construct function
    public BaseMessage(Object content)
    {
        this.sendTime = new Date();
        this.content = content;
    }

    // Access Function
    public Date getSendTime()
    {
        return this.sendTime;
    }

    public Object getContent()
    {
        return this.content;
    }
}
