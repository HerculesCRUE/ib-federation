package es.um.asio.service.test;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.catalina.util.URLEncoder;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.junit.Test;


import java.io.IOException;
import java.nio.charset.Charset;

public class Test_1 {

    @Test
    public void test1() throws IOException, EncoderException {
        String ul = new URLEncoder().encode("HOLA MUNDO", Charset.defaultCharset());
        System.out.println(ul);
        System.out.println(new URLCodec().encode("Hola Mundo"));
        System.out.println(java.net.URLEncoder.encode("Hello World", "UTF-8").replace("+", "%20"));

    }

}
