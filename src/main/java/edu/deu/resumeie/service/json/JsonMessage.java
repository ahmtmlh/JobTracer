package edu.deu.resumeie.service.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface JsonMessage {
    Map<String, List<String>> arrays = new HashMap<>();

    void addItem(String key, Object item);
    boolean addArray(String arrayName);
    boolean addItemToArray(String arrayName, Object item);
    void removeItem(String key);
    boolean removeItem(String arrayName, Object item);
    List<String> getArray(String arrayName);

    String getArrayAsString(String arrayName);
    String build();
}
