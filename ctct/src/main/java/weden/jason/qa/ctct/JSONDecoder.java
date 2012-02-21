package weden.jason.qa.ctct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JSONDecoder {
    private static final Logger LOG = LogManager.getLogger(JSONDecoder.class);

    protected Map<String, String> parse(String jsonText) {
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory() {
            public List creatArrayContainer() {
                return new LinkedList();
            }

            public Map<String, String> createObjectContainer() {
                return new LinkedHashMap();
            }
        };

        Map<String, String> json = null;
        try {
            json = (Map<String, String>) parser.parse(jsonText, containerFactory);
        } catch (ParseException pe) {
            LOG.error("Error parsing json");
        }
        return json;
    }
}
