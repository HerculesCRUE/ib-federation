package es.um.asio.service.test;



import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.codec.EncoderException;

import org.json.JSONObject;
import org.json.XML;
import org.json.JSONException;
import org.junit.Test;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test_1 {

    @Test
    public void test1() throws IOException, EncoderException {
        int PRETTY_PRINT_INDENT_FACTOR = 4;
        String TEST_XML_STRING =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                        "<!DOCTYPE eSearchResult PUBLIC \"-//NLM//DTD esearch 20060628//EN\" \"https://eutils.ncbi.nlm.nih.gov/eutils/dtd/20060628/esearch.dtd\">\n" +
                        "<eSearchResult><Count>1</Count><RetMax>1</RetMax><RetStart>0</RetStart><IdList>\n" +
                        "<Id>27479999</Id>\n" +
                        "</IdList><TranslationSet/><TranslationStack>   <TermSet>    <Term>Building[Title]</Term>    <Field>Title</Field>    <Count>23001</Count>    <Explode>N</Explode>   </TermSet>   <TermSet>    <Term>genomic[Title]</Term>    <Field>Title</Field>    <Count>47894</Count>    <Explode>N</Explode>   </TermSet>   <OP>AND</OP>   <TermSet>    <Term>nation[Title]</Term>    <Field>Title</Field>    <Count>3412</Count>    <Explode>N</Explode>   </TermSet>   <OP>AND</OP>   <OP>GROUP</OP>   <TermSet>    <Term>Brasilis'[Title]</Term>    <Field>Title</Field>    <Count>1</Count>    <Explode>N</Explode>   </TermSet>   <TermSet>    <Term>Mexicano'[Title]</Term>    <Field>Title</Field>    <Count>1</Count>    <Explode>N</Explode>   </TermSet>   <OP>AND</OP>   <TermSet>    <Term>comparative[Title]</Term>    <Field>Title</Field>    <Count>171988</Count>    <Explode>N</Explode>   </TermSet>   <OP>AND</OP>   <TermSet>    <Term>cultural[Title]</Term>    <Field>Title</Field>    <Count>22510</Count>    <Explode>N</Explode>   </TermSet>   <OP>AND</OP>   <TermSet>    <Term>perspective[Title]</Term>    <Field>Title</Field>    <Count>75451</Count>    <Explode>N</Explode>   </TermSet>   <OP>AND</OP>   <OP>GROUP</OP>   <OP>AND</OP>   <OP>GROUP</OP>  </TranslationStack><QueryTranslation>(Building[Title] AND genomic[Title] AND nation[Title]) AND (Brasilis'[Title] AND Mexicano'[Title] AND comparative[Title] AND cultural[Title] AND perspective[Title])</QueryTranslation><ErrorList><PhraseNotFound>'Homo</PhraseNotFound><PhraseNotFound>'Genoma</PhraseNotFound></ErrorList><WarningList><PhraseIgnored>the</PhraseIgnored><PhraseIgnored>and</PhraseIgnored><PhraseIgnored>in</PhraseIgnored></WarningList></eSearchResult>";
        try {
            JsonElement jObject = new Gson().fromJson(XML.toJSONObject(TEST_XML_STRING).toString(),JsonElement.class);
            System.out.println(jObject);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
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
