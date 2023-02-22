package Message.XMLMessage;

import java.io.*;

public class Conversion
{
    public byte[] messageToByte(XMLMessage message) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        oos.close();
        bos.close();
        return bytes;
    }

    public XMLMessage byteToMessage(byte[] bytes) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        XMLMessage message = (XMLMessage)ois.readObject();

        bis.close();
        ois.close();
        return message;
    }
}