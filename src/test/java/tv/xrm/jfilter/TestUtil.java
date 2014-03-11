package tv.xrm.jfilter;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

final class TestUtil {

    private TestUtil() {
    }

    static final ObjectMapper MAPPER = new ObjectMapper();

    static JsonNode readSampleJson(String name) {
        URL jsonUrl = Thread.currentThread().getContextClassLoader().getResource(name);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonUrl, JsonNode.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static String q(String singleQuoted) {
        return singleQuoted.replace('\'', '"');
    }
}
