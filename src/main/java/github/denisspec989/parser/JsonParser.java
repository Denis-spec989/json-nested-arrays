package github.denisspec989.parser;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import github.denisspec989.dto.JsonRow;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class JsonParser {

    public static List<JsonRow> parseJson(InputStream inputStream) {
        DocumentContext jsonContext = JsonPath.parse(inputStream);
        return extractJsonRows("", jsonContext.json());
    }

    public static List<JsonRow> extractJsonRows(String currentPath, Object jsonValue) {
        List<JsonRow> result = new ArrayList<>();

        if (jsonValue instanceof List) {
            List<Object> jsonArray = (List<Object>) jsonValue;
            for (int i = 0; i < jsonArray.size(); i++) {
                result.addAll(extractJsonRows(currentPath + "[" + i + "]", jsonArray.get(i)));
            }
        } else if (jsonValue instanceof  java.util.LinkedHashMap) {
            java.util.LinkedHashMap<String, Object> jsonObj = (java.util.LinkedHashMap<String, Object>) jsonValue;
            for (String key : jsonObj.keySet()) {
                result.addAll(extractJsonRows(currentPath + "." + key, jsonObj.get(key)));
            }
        } else {
            result.add(new JsonRow("$"+currentPath, jsonValue));
        }

        return result;
    }


    public static boolean matchesFilterWithReplacement(JsonRow jsonRow, String filter, String newJsonPath) {
            String[] jsonPathParts = jsonRow.getJsonPath().split("\\.");
            String[] filterParts = filter.split("\\.");

            if (jsonPathParts.length != filterParts.length) {
                    return false;
                }

            for (int i = 0; i < jsonPathParts.length; i++) {
                jsonPathParts[i]=removeSquareBrackets(jsonPathParts[i]);
                filterParts[i]=removeSquareBrackets(filterParts[i]);
                    if (!jsonPathParts[i].equals(filterParts[i])) {
                            return false;
                        }
                }
            newJsonPath = fillString(jsonRow.getJsonPath(),newJsonPath);
            jsonRow.setJsonPath(newJsonPath);
            return true;
        }
    public static boolean matchesAnyFilterWithReplacement(JsonRow jsonRow, Map<String,String> filters) {
            for (String filter : filters.keySet()) {
                    if (matchesFilterWithReplacement(jsonRow, filter,filters.get(filter))) {
                            return true;
                        }
                }
            return false;
        }
    public static List<JsonRow> filterJsonRowsWithReplacement(List<JsonRow> jsonRows, Map<String,String> filtersWithReplacement) {
        return jsonRows.stream()
                .filter(row -> matchesAnyFilterWithReplacement(row, filtersWithReplacement))
                .collect(Collectors.toList());
    }
    public static String removeSquareBrackets(String input) {
        StringBuilder sb = new StringBuilder();
        boolean insideBrackets = false;

        for (char c : input.toCharArray()) {
            if (c == '[') {
                insideBrackets = true;
            }
            if (!insideBrackets) {
                sb.append(c);
            }
            if (c == ']') {
                insideBrackets = false;
            }
        }

        return sb.toString();
    }
    public static String fillString(String inputStr, String outputStr) {
        String[] inputValues = extractValues(inputStr);

        for (int i = 0; i < inputValues.length; i++) {
            if(outputStr.contains("[]")){
                outputStr = outputStr.replaceFirst("\\[\\]", "[" + inputValues[i] + "]");
            }else {
                outputStr = outputStr.replaceFirst("\\[\\*\\]", "[" + inputValues[i] + "]");
            }

        }

        return outputStr;
    }

    public static String[] extractValues(String str) {
        List<String> values = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            values.add(matcher.group(1));
        }

        return values.toArray(new String[0]);
    }

}
