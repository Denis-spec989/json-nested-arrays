package github.denisspec989.creator;

import github.denisspec989.dto.JsonRow;
import github.denisspec989.dto.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonCreator {
    public static final String ROOT_ELEMENT = "$";
    public static final String ARRAY_REG_EXP = "(.+)\\[(\\d+)]";
    private static final Pattern PATTERN = Pattern.compile(ARRAY_REG_EXP);

    public String create(List<JsonRow> jsonRowList) {
        return merge(new JSONObject(), jsonRowList);
    }

    public String merge(Object object, List<JsonRow> jsonRowList) {
        JSONObject prepareObj;
        if (object instanceof JSONObject) {
            prepareObj = (JSONObject) object;
        } else if (object instanceof String) {
            prepareObj = new JSONObject((String) object);
        } else {
            prepareObj = new JSONObject(object);
        }
        JSONObject jsonStructure = mergeJsonStructure(prepareObj, jsonRowList);
        return jsonStructure.toString();
    }

    private JSONObject mergeJsonStructure(JSONObject jsonObject, List<JsonRow> jsonRowList) {
        if (jsonRowList != null) {
            for (JsonRow row : jsonRowList) {
                mergeJsonRow(jsonObject, row);
            }
        }
        return jsonObject;
    }

    private void mergeJsonRow(JSONObject rootNode, JsonRow jsonRow) {
        String[] paths = splitPath(jsonRow.getJsonPath());
        if (paths.length == 0) return;

        Pair<String, Integer> arrayMeta = getArrayMeta(paths[0]);
        Object parentNode = findParent(rootNode, arrayMeta);
        int startIndex = 0;
        int parentArrayIndex;
        if (parentNode == null) {
            parentNode = rootNode;
            parentArrayIndex = -1;
        } else {
            startIndex++;
            parentArrayIndex = arrayMeta.getValue();
        }
        Object activeNode;
        int arrayIndex;
        for (int i = startIndex; i < paths.length; i++) {
            String path = paths[i];
            arrayMeta = getArrayMeta(path);
            path = arrayMeta.getKey();
            arrayIndex = arrayMeta.getValue();

            activeNode = null;
            if (arrayIndex == -1 && parentNode instanceof  JSONObject){
                activeNode = ((JSONObject) parentNode).optJSONObject(path);
            } else {
                if(parentNode instanceof  JSONObject){
                    activeNode = ((JSONObject) parentNode).opt(path);
                }else if( parentNode instanceof  JSONArray){
                    activeNode = ((JSONArray) parentNode).optJSONObject(parentArrayIndex);
                    if(activeNode !=null){
                        parentNode = activeNode;
                        activeNode = ((JSONObject)activeNode).opt(path);
                    }
                }
            }
            if(activeNode == null){
                if(i == paths.length-1){
                    Object value = jsonRow.getValue();
                    putToNode(parentNode,path,parentArrayIndex,value);
                }else {
                    activeNode = putToNode(parentNode,parentArrayIndex,path,arrayIndex);
                }
            }
            if(activeNode !=null){
                parentNode = activeNode;
                parentArrayIndex = arrayIndex;
            }

        }
    }

    private String[] splitPath(String jsonPath) {
        return jsonPath.split("\\.");
    }

    private boolean isArrayElement(String path) {
        return path.matches(ARRAY_REG_EXP);
    }

    private Pair<String, Integer> getArrayMeta(String path) {
        Matcher match = PATTERN.matcher(path);
        if (match.find()) {
            return new Pair<>(match.group(1), Integer.parseInt(match.group(2)));
        }
        return new Pair<>(path, -1);
    }

    private Object findParent(JSONObject rootNode, Pair<String, Integer> arrayMeta) {
        String path = arrayMeta.getKey();
        Object parentNode;
        if (ROOT_ELEMENT.equals(path)) {
            parentNode = rootNode;
        } else {
            int index = arrayMeta.getValue();
            parentNode = findNode(rootNode, path, index);
        }
        return parentNode;
    }

    private JSONObject findNode(JSONObject node, String name, int index) {
        Set<String> keys = node.keySet();
        if (keys.contains(name)) {
            Object obj = node.opt(name);
            if (obj instanceof JSONObject) {
                return (JSONObject) obj;
            } else if (obj instanceof JSONArray) {
                return ((JSONArray) obj).optJSONObject(index);
            }
        }
        for (String key : keys) {
            final JSONObject objByKey = node.optJSONObject(key);
            if (objByKey != null) {
                JSONObject foundObj = findNode(objByKey, name, index);
                if (foundObj != null) {
                    return foundObj;
                }
            }
        }
        return null;
    }
    private void  putToNode(Object node,String key, int index, Object value){
        if(value == null) {
            value = JSONObject.NULL;
        }
        if(node instanceof JSONObject){
            ((JSONObject) node).put(key,value);
        } else if (node instanceof JSONArray) {
            JSONObject obj = new JSONObject();
            obj.put(key, value);
            ((JSONArray) node).put(index,obj);
        }
    }
    private Object putToNode(Object node, int parentIndex, String key, int index){
        Object newNode = index > -1 ? new JSONArray(): new JSONObject();
        putToNode(node,key,parentIndex,newNode);
        return newNode;
    }
}
