package Producer;

import org.dom4j.Document;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import Message.XMLMessage.Conversion;
import Message.XMLMessage.XMLMessage;

import javax.swing.*;

public class Producer
{
    private final String docName;
    private final Document document;
    private final String userID;
    private final String modelType;

    // rabbitmq config
    private final String QUEUE_NAME;
    private final String host;
    private final int port;
    private final String virtualHost;
    private final String userName;
    private final String userPassword;

    // Construction function
    public Producer(String QUEUE_NAME, String docName, Document document, String host, int port,
                    String virtualHost, String userName, String userPassword, String userID, String modelType)
    {
        this.QUEUE_NAME = QUEUE_NAME;
        this.document = document;
        this.docName = docName;
        this.userID = userID;
        this.modelType = modelType;

        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public void send() throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setPort(this.port);
        factory.setVirtualHost(this.virtualHost);
        factory.setUsername(this.userName);
        factory.setPassword(this.userPassword);

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel())
        {
            Conversion conversion = new Conversion();
            XMLMessage message = new XMLMessage(this.userID, this.docName, this.modelType, this.document);
            byte[] bytes = conversion.messageToByte(message);
            System.out.println("Start transferring message");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, bytes);
            String result = "The message has been sent.(size: " + bytes.length + " bytes)";
            JOptionPane.showMessageDialog(null, result, "XML Generation", JOptionPane.PLAIN_MESSAGE);
        }
    }
}
