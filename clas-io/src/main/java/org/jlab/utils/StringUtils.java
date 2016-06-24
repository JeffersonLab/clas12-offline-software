package org.jlab.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class StringUtils {
	
	public static long calculateChecksum(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
		    byte[] digest = md.digest(s.getBytes());
		    return ByteBuffer.wrap(digest).getLong();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return (long)0;
	}

    @SuppressWarnings("unused")
    private static String readFileAsString(String file_name)
            throws IOException
    {
        return StringUtils.readFileAsString(file_name, null);
    }

    private static String readFileAsString(String file_name, String charset_name) throws java.io.IOException
    {
        java.io.InputStream is = new java.io.FileInputStream(file_name);
        try
        {
            final int bufsize = 4096;
            int available = is.available();
            byte[] data = new byte[available < bufsize ? bufsize
                    : available];
            int used = 0;
            while (true)
            {
                if (data.length - used < bufsize)
                {
                    byte[] newData = new byte[data.length << 1];
                    System.arraycopy(data, 0, newData, 0, used);
                    data = newData;
                }
                int got = is.read(data, used, data.length - used);
                if (got <= 0)
                    break;
                used += got;
            }
            return charset_name != null ? new String(data, 0, used,
                    charset_name) : new String(data, 0, used);
        }
        finally
        {
            is.close();
        }
    }
    
    public static String xmlToString(Document doc) throws TransformerFactoryConfigurationError, TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
    }
}
