package com.soconnor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.soconnor.AvroSchemaInferenceUtils.inferAvroSchema;

public class AvroSchemaInferenceUtilsTest {

    @Before
    public void setup() {
        //if we don't call below, we will get NullPointerException
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSchemaInference() throws IOException
    {
        String data = new String(Files.readAllBytes(Paths.get("src/test/resources/schema/test-msg.json")));
        String schema = inferAvroSchema(data);
        System.out.println(schema);
    }

    @Test
    public void testEmptyArray() throws IOException
    {
        String data = new String(Files.readAllBytes(Paths.get("src/test/resources/schema/empty-array.json")));
        String schema = inferAvroSchema(data);
        System.out.println(schema);
    }

    @Test
    public void testArrayOfArray() throws IOException
    {
        String data = new String(Files.readAllBytes(Paths.get("src/test/resources/schema/array-of-array.json")));
        String schema = inferAvroSchema(data);
        System.out.println(schema);
    }
}
