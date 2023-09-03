package com.soconnor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Iterator;
import java.util.Map;

public class AvroSchemaInferenceUtils {

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ARRAY = "array";
    private static final String ITEMS = "items";
    private static final String STRING = "string";
    private static final String RECORD = "record";
    private static final String FIELDS = "fields";
    private static final String NULL = "null";
    private static final String BOOLEAN = "boolean";

    static Logger logger = LoggerFactory.getLogger(AvroSchemaInferenceUtils.class);
    static ObjectMapper mapper = new ObjectMapper();

    //http://www.dataedu.ca/avro#:~:text=Infer%20Avro%20schema%20from%20JSON,the%20items%20of%20the%20Array.
    //https://www.tutorialspoint.com/avro/avro_schemas.htm

    //This function returns only the array of fields.  If we need the full AVRO schema see commented out code
    public static String inferAvroSchema(final String json) {
        String schema = null;
        try {
            //namespace should be something like "com.sharon.dataeng"
            final JsonNode jsonNode = mapper.readTree(json);
            //final ObjectNode avroSchema = mapper.createObjectNode();
//            avroSchema.put(TYPE, RECORD);
//            avroSchema.put("namespace", topic.getNamespace());
            //avroSchema.set(FIELDS, getFields(jsonNode));
            //schema = avroSchema.toString();
            schema = getFields(jsonNode).toString();
        } catch (Exception e) {
            logger.info("Unable to infer schema : " + e.getMessage());
        }
        return schema;
    }

    private static ArrayNode getFields(final JsonNode jsonNode) {
        final ArrayNode fields = mapper.createArrayNode();
        final Iterator<Map.Entry<String, JsonNode>> elements = jsonNode.fields();

        Map.Entry<String, JsonNode> map;
        while (elements.hasNext()) {
            map = elements.next();
            final JsonNode nextNode = map.getValue();

            switch (nextNode.getNodeType()) {
                case NUMBER:
                    //Avro schema only has int, long, float, double.
                    //Using long and double since this schema is just displayed in Alation
                    String numType = "double";
                    if (nextNode.isInt() || nextNode.isLong()) {
                        numType = "long";
                    }

                    fields.add(mapper.createObjectNode().put(NAME, map.getKey()).put(TYPE, numType));
                    break;

                case STRING:
                    fields.add(mapper.createObjectNode().put(NAME, map.getKey()).put(TYPE, STRING));
                    break;

                case ARRAY:
                    final ArrayNode arrayNode = (ArrayNode) nextNode;
                    final JsonNode element = arrayNode.get(0);
                    final ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put(NAME, map.getKey());

                    if (element == null) {
                        break;
                    }
                    if (element.getNodeType() == JsonNodeType.NUMBER) {
                        String arrayNumType = "double";
                        if (element.isInt() || element.isLong()) {
                            arrayNumType = "long";
                        }
                        objectNode.set(TYPE, mapper.createObjectNode().put(TYPE, ARRAY).put(ITEMS, arrayNumType));
                        fields.add(objectNode);
                    } else if (element.getNodeType() == JsonNodeType.STRING) {
                        objectNode.set(TYPE, mapper.createObjectNode().put(TYPE, ARRAY).put(ITEMS, STRING));
                        fields.add(objectNode);
                    } else {
                        objectNode.set(TYPE,
                                mapper.createObjectNode().put(TYPE, ARRAY).set(ITEMS, mapper.createObjectNode()
                                        .put(TYPE, RECORD).put(NAME, getName(map)).set(FIELDS, getFields(element))));
                    }
                    break;

                case OBJECT:
                    ObjectNode node = mapper.createObjectNode();
                    node.put(TYPE, RECORD).put(NAME, getName(map)).set(FIELDS, getFields(nextNode));
                    fields.add(node);
                    break;

                case NULL:
                    fields.add(mapper.createObjectNode().put(NAME, map.getKey()).put(TYPE, NULL));
                    break;

                case BOOLEAN:
                    fields.add(mapper.createObjectNode().put(NAME, map.getKey()).put(TYPE, BOOLEAN));
                    break;

                default:
                    logger.error("Node type not found - " + nextNode.getNodeType());
                    throw new RuntimeException("Unable to determine action for ndoetype " + nextNode.getNodeType()
                            + "; Allowed types are ARRAY, STRING, NUMBER, OBJECT");
            }
        }
        return fields;
    }

    private static String getName(Map.Entry<String, JsonNode> map) {
        return (map.getKey());
    }
}
