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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test_1 {

    @Test
    public void test1() throws IOException, EncoderException {
        String input = "SELECT ?a ?b ?c WHERE { ?a ?b ?c } LIMIT 1000";
        String other = input.replaceAll("(?i)limit\\s+(\\d+)","");

        System.out.println(getLimitInSPARQL(input));
    }

    private Integer getLimitInSPARQL(String query) {
        String pattern = "(?i)limit\\s+(\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(query);

        while (m.find()) {
            return Integer.valueOf(m.group(1));
        }
        return null;
    }

}
