package Consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import Message.XMLMessage.Conversion;
import Message.XMLMessage.SubClass.DocumentInfo;
import Message.XMLMessage.SubClass.UserInfo;
import Message.XMLMessage.XMLMessage;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Consumer
{
    private final String QUEUE_NAME;
    private final String host;
    private Document document;

    public Consumer(String QUEUE_NAME, String host)
    {
        this.QUEUE_NAME = QUEUE_NAME;
        this.host = host;
        this.document = null;
    }

    public void recv() throws Exception
    {
        Conversion conversion = new Conversion();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(this.QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) ->
        {
            byte[] bytes = delivery.getBody();
            System.out.println(" [x] Received 1 message(size: " + bytes.length + " bytes)");

            try
            {
                XMLMessage message = conversion.byteToMessage(bytes);
                UserInfo userMessage = message.getUserInfo();
                DocumentInfo xmlMessage = message.getDocumentInfo();
                this.document = xmlMessage.getDocument();
                String docName = xmlMessage.getDocName();
                XMLGeneration(docName);

                System.out.println("XML Document has been generated.");
                System.out.println("=====================================");
                System.out.println("User ID: " + userMessage.getUserID());
                System.out.println("Doc Name: " + xmlMessage.getDocName());
                System.out.println("Model Type: " + xmlMessage.getModelType());
                System.out.println("Time: " + xmlMessage.getTime());
                System.out.println("=====================================");
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        };
        channel.basicConsume(this.QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }

    public void XMLGeneration(String docName)
    {
        // Format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");

        /*
         * Generate XML file
         * The file is located in the root path of the project
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println("File generation failed! File name: " + docName);
        }

        try (FileOutputStream fos = new FileOutputStream(docName + ".xml"))
        {
            fos.write(out.toByteArray());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
