package edu.deu.resumeie.service.json;

import java.util.List;

public interface JsonMessage {
    void addItem(String key, Object item);
    boolean addArray(String arrayName);
    boolean addItemToArray(String arrayName, Object item);
    void removeItem(String key);
    boolean removeItem(String arrayName, Object item);
    List<String> getArray(String arrayName);

    String getArrayAsString(String arrayName);
    String build();
}
