package github.denisspec989.creator;

import github.denisspec989.dto.JsonRow;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JsonCreatorTest {
    private static final String JSON_CREATOR_TABLE_ID = "JsonCreator";
    private final JsonCreator jsonCreator = new JsonCreator();
    @Test
    @DisplayName("Generate JSON from dictionary")
    void jsonObjectCreatorTest(){
        List<JsonRow> jsonRows = new ArrayList<>();
        jsonRows.add(new JsonRow("$.entry.value",5));
        jsonRows.add(new JsonRow("entry.type","lalalalla"));
        jsonRows.add(new JsonRow("entry.array[0].value",0.61237));
        String jsonString = jsonCreator.create(jsonRows);
        assertEquals("{\"entry\":{\"array\":[{\"value\":0.61237}],\"type\":\"lalalalla\",\"value\":5}}",jsonString);
    }

}
