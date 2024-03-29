package github.denisspec989.parser;

import github.denisspec989.dto.JsonRow;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {
    @Test
    public void test_parse_valid_json_input_stream() throws Exception {
        // Arrange
        String json = "{\"key\":\"value\",\"key2\":[1,2,3]}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        // Act
        List<JsonRow> jsonRows = JsonParser.parseJson(inputStream);

        // Assert
        assertNotNull(jsonRows);
        assertEquals(4, jsonRows.size());
        assertEquals(new JsonRow("$.key", "value"), jsonRows.get(0));
        assertEquals(new JsonRow("$.key2[0]", 1), jsonRows.get(1));
        assertEquals(new JsonRow("$.key2[1]", 2), jsonRows.get(2));
        assertEquals(new JsonRow("$.key2[2]", 3), jsonRows.get(3));
    }
    @Test
    public void test_handle_json_input_stream_with_null_values() throws Exception {
        // Arrange
        String json = "{\"key1\":null, \"key2\":{\"subKey1\":null}}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        // Act
        List<JsonRow> jsonRows = JsonParser.parseJson(inputStream);

        // Assert
        assertNotNull(jsonRows);
        assertEquals(2, jsonRows.size());
        assertEquals("$.key1", jsonRows.get(0).getJsonPath());
        assertNull(jsonRows.get(0).getValue());
        assertEquals("$.key2.subKey1", jsonRows.get(1).getJsonPath());
        assertNull(jsonRows.get(1).getValue());
    }
    @Test
    public void test_parts_match() {
        JsonRow jsonRow = new JsonRow("$.path.to.value[3]", "value");
        String filter = "$.path.to.value[]";
        String newJsonPath = "$.new.path.to.value[]";

        boolean result = JsonParser.matchesFilterWithReplacement(jsonRow, filter, newJsonPath);

        assertTrue(result);
        assertEquals("$.new.path.to.value[3]", jsonRow.getJsonPath());
    }
    @Test
    public void test_returns_false_if_filters_map_is_empty() {
        // Arrange
        JsonRow jsonRow = new JsonRow("jsonPath", "value");
        Map<String, String> filters = new HashMap<>();

        // Act
        boolean result = JsonParser.matchesAnyFilterWithReplacement(jsonRow, filters);

        // Assert
        assertFalse(result);
    }
    @Test
    public void test_returns_true_if_any_filter_matches() {
        // Arrange
        JsonRow jsonRow = new JsonRow("$.result.riskMetricData[1]", "value");
        Map<String, String> filters = new HashMap<>();
        filters.put("$.result.riskMetricData[0]", "replacement");
        filters.put("$.result.abvgdsdsd","replacement_second");

        // Act
        boolean result = JsonParser.matchesAnyFilterWithReplacement(jsonRow, filters);

        // Assert
        assertTrue(result);
    }
    @Test
    public void test_filtered_rows_by_filters() {
        String jsonString = "{\n" +
                "  \"result\": [\n" +
                "    {\n" +
                "      \"id_corpus\": \"in sint esse\",\n" +
                "      \"cost_1m\": -1272.006,\n" +
                "      \"sale_distrib\": [\n" +
                "        {\n" +
                "          \"year\": 2019,\n" +
                "          \"quarter\": 1,\n" +
                "          \"sale_pct\": 1,\n" +
                "          \"sale_square\": 1\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id_corpus\": \"quis amet tempor\",\n" +
                "      \"cost_1m\": 1375,\n" +
                "      \"sale_distrib\": [\n" +
                "        {\n" +
                "          \"year\": 2018,\n" +
                "          \"quarter\": 3,\n" +
                "          \"sale_pct\": 3,\n" +
                "          \"sale_square\": 3\n" +
                "        },\n" +
                "        {\n" +
                "          \"year\": 2017,\n" +
                "          \"quarter\": 4,\n" +
                "          \"sale_pct\": 4,\n" +
                "          \"sale_square\": 4\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        List<JsonRow> jsonRows = JsonParser.parseJson(new ByteArrayInputStream(jsonString.getBytes()));
        HashMap<String,String> filtersWithReplacement = new HashMap<>();
        filtersWithReplacement.put("$.result[].id_corpus","$.result.riskMetricData.limitFzhnData.buildings[].id");
        filtersWithReplacement.put("$.result[].sale_distrib[].sale_square","$.result.riskMetricData.limitFzhnData.buildings[].saleDistrib[].saleSquare");
        List<JsonRow> filteredRows = JsonParser.filterJsonRowsWithReplacement(jsonRows, filtersWithReplacement);

        assertEquals(5, filteredRows.size());
        assertTrue(filteredRows.stream().anyMatch(row->"$.result.riskMetricData.limitFzhnData.buildings[0].id".equals(row.getJsonPath())));
        assertTrue(filteredRows.stream().anyMatch(row->"$.result.riskMetricData.limitFzhnData.buildings[0].saleDistrib[0].saleSquare".equals(row.getJsonPath())));
    }

}