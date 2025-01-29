package edu.kaist.corus.nlp.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Junseok Park on 2017-07-13.
 */
public class NERResult {
    private JsonNode JSONResult;

    public NERResult(String factorJsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNodeObj = mapper.readTree(factorJsonString);
        this.setJSONResult(jsonNodeObj);
    }

    public JsonNode getJSONResult() {
        return JSONResult;
    }

    public void setJSONResult(JsonNode JSONResult) {
        this.JSONResult = JSONResult;
    }
}
