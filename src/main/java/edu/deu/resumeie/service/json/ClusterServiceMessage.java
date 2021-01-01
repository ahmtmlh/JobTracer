package edu.deu.resumeie.service.json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

public class ClusterServiceMessage implements JsonMessage{

    private static final Logger logger = LogManager.getLogger(ClusterServiceMessage.class);

    // Arrays
    private final Map<String, List<String>> arrays;

    //Json part
    private final JSONObject root;

    // String part
    private boolean changed;
    private String stringForm;

    public ClusterServiceMessage(){
        changed = false;
        stringForm = "";
        root = new JSONObject();
        arrays = new HashMap<>();
    }

    public void addItem(String key, Object item){
        root.put(key, item.toString());
    }

    @Override
    public boolean addArray(String arrayName) {
        if(arrays.containsKey(arrayName)) return false;
        arrays.put(arrayName, new ArrayList<>());
        return true;
    }

    @Override
    public boolean addItemToArray(String arrayName, Object item) {
        boolean ret = false;
        if(arrays.containsKey(arrayName)){
            ret = arrays.get(arrayName).add(item.toString());
            changed = true;
        }

        return ret;
    }

    public void removeItem(String key){
        root.remove(key);
    }

    @Override
    public boolean removeItem(String arrayName, Object item) {
        if(arrays.containsKey(arrayName)){
            changed = true;
            return arrays.get(arrayName).remove(item.toString());
        }
        return false;
    }

    @Override
    public String getArrayAsString(String arrayName) {
        if (arrays.containsKey(arrayName)){
            String str = Arrays.toString(arrays.get(arrayName).toArray());
            return str.substring(1, str.length()-1);
        }
        return null;
    }

    @Override
    public List<String> getArray(String arrayName) {
        return arrays.get(arrayName);
    }

    @Override
    public String build(){
        if(changed){
            changed = false;
            buildJson();
        }
        return stringForm;
    }

    private void buildJson() {
        for(Map.Entry<String, List<String>> entry : arrays.entrySet()){
            JSONArray array = new JSONArray();
            for(String str : entry.getValue()){
                JSONObject temp = new JSONObject();
                temp.put("value", str);
                array.put(temp);
            }
            root.put(entry.getKey(), array);
        }
        stringForm = root.toString();
    }

    /**
     * This function will return messages array as a comma separated string.
     * @return Messages as a comma separated string
     */
    public String getMessageString(){
        return getArrayAsString("messages");
    }

    public static Optional<ClusterServiceMessage> parse(String jsonString){
        ClusterServiceMessage message = new ClusterServiceMessage();
        List<String> messages = new ArrayList<>();
        try{
            JSONTokener tokens = new JSONTokener(jsonString);
            JSONObject root = new JSONObject(tokens);
            int count = root.getInt("count");
            if (count > 0){
                JSONArray array = root.getJSONArray("clusters");
                for (int i = 0; i < count; i++) {
                    messages.add(String.valueOf(array.getJSONObject(i).get("value")));
                }
            }
            message.addArray("clusters");
            message.getArray("clusters").addAll(messages);
            message.addItem("count", count);
            message.stringForm = jsonString;
        } catch (JSONException e){
            logger.error(e.getLocalizedMessage(), e);
        }
        return Optional.of(message);
    }

    @Override
    public String toString() {
        return this.build();
    }
}
