package com.techx.tradex.ekycadmin.models.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.techx.tradex.ekycadmin.models.request.EKycAddReq;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EKycAddReqDeserializer extends StdDeserializer<EKycAddReq> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public EKycAddReqDeserializer() {
        super(EKycAddReq.class);
    }

    @Override
    public EKycAddReq deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return deserializeNode(node, new EKycAddReq());
    }

    /**
     * Get all fields including inherited fields from parent classes
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        
        return fields;
    }

    public EKycAddReq deserializeNode(JsonNode node, EKycAddReq instance) {
        getAllFields(EKycAddReq.class)
            .stream()
            .forEach(
                field -> {
                    try {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        JsonNode fieldNode = node.get(fieldName);
                        if (fieldNode != null) {
                            if (fieldNode.isTextual() && !fieldName.equals("dataSign") && !fieldName.equals("dataBase64")) {
                                Object value = fieldNode.asText().replaceAll(System.lineSeparator(), " ").trim();
                                value = objectMapper.convertValue(value, field.getType());
                                field.set(instance, value);
                            } else if (fieldNode.isArray()) {
                                Class<?> elementType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                List<?> valueList = objectMapper.readValue(fieldNode.traverse(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
                                field.set(instance, valueList);
                            } else {
                                Object value = objectMapper.treeToValue(fieldNode, field.getType());
                                field.set(instance, value);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error deserializing field: " + field.getName(), e);
                    }
                }
            );
        return instance;
    }
}
