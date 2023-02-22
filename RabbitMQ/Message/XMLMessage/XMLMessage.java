package Message.XMLMessage;

import Message.XMLMessage.SubClass.DocumentInfo;
import Message.XMLMessage.SubClass.UserInfo;
import org.dom4j.Document;

import java.io.Serializable;

public class XMLMessage implements Serializable
{
    private final UserInfo userInfo;
    private final DocumentInfo documentInfo;

    // Construct function
    public XMLMessage(String userID, String docName, String modelType, Document document)
    {
        this.userInfo = new UserInfo(userID);
        this.documentInfo = new DocumentInfo(docName, modelType, document);
    }

    // Access Function
    public UserInfo getUserInfo()
    {
        return this.userInfo;
    }

    public DocumentInfo getDocumentInfo()
    {
        return documentInfo;
    }
}
