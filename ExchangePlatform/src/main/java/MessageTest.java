import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class MessageTest {

    public static void main(String[] args) throws IOException {

        String actualMessage = "+-------------------------+-----------------------------+\n" +
                "\n" +
                "|Key                      |           Values            |\n" +
                "\n" +
                "+-------------------------+-----------------------------+\n" +
                "\n" +
                "|account                  |            BATS             |\n" +
                "\n" +
                "|clordid                  |    restingSellOrder_GVB     |\n" +
                "\n" +
                "|custorderhandlinginst    |              Y              |\n" +
                "\n" +
                "|executionid              |         S100000FE0          |\n" +
                "\n" +
                "|executiontype            |              4              |\n" +
                "\n" +
                "|lastshares               |              0              |\n" +
                "\n" +
                "|operatorid               |             CFE             |\n" +
                "\n" +
                "|orderid                  |        172H1JDVCPVW         |\n" +
                "\n" +
                "|orderquantity            |             10              |\n" +
                "\n" +
                "|price                    |            11.00            |\n" +
                "\n" +
                "|securitytype             |             FUT             |\n" +
                "\n" +
                "|sequencenumber           |            1822             |\n" +
                "\n" +
                "|side                     |              2              |\n" +
                "\n" +
                "|symbol                   |           000001            |\n" +
                "\n" +
                "|targetcomputerid         |            FOOA             |\n" +
                "\n" +
                "|                  |            BATS             |\n" +
                "\n" +
                "|clordid                  |            None             |\n" +
                "\n" +
                "|targetsubid              |            0001             |\n" +
                "\n" +
                "|transacttime             | 2022-02-02T15:12:02.979000  |\n" +
                "\n" +
                "+-------------------------+-----------------------------+";

        String expectedMessage = "+-------------------------+-----------------------------+\n" +
                "\n" +
                "|Key                      |           Values            |           \n" +
                "\n" +
                "+-------------------------+-----------------------------+\n" +
                "\n" +
                "|account                  |            BATS             |\n" +
                "\n" +
                "|clordid                  |            None             |\n" +
                "\n" +
                "|custorderhandlinginst    |              Y              |\n" +
                "\n" +
                "|executionid              |         S100000FDR          |\n" +
                "\n" +
                "|executiontype            |              4              |\n" +
                "\n" +
                "|lastshares               |              0              |\n" +
                "\n" +
                "|operatorid               |             148             |\n" +
                "\n" +
                "|orderid                  |        172H1JDVCPVW         |\n" +
                "\n" +
                "|orderquantity            |             10              |\n" +
                "\n" +
                "|price                    |            11.00            |\n" +
                "\n" +
                "|securitytyp             |             FUT             |\n" +
                "\n" +
                "|sequencenumber           |              0              |\n" +
                "\n" +
                "|side                     |              2              |\n" +
                "\n" +
                "|symbol                   |           000001            |\n" +
                "\n" +
                "|targetcomputerid         |            FOOA             |\n" +
                "\n" +
                "|targetsubid              |             ''              |\n" +
                "\n" +
                "|transacttime             |            None             |\n" +
                "\n" +
                "+-------------------------+-----------------------------+";

        Map<String,List<String>> differences = valueComparator(expectedMessage, actualMessage);
        System.out.println(differences);
        pushToExcel(differences);

    }

    public static Map<String, List<String>> valueComparator(String expectedMessage, String actualMessage) {

        Map<String, String> expectedMap = extractKeyValue(expectedMessage);
        Map<String, String> actualMap = extractKeyValue(actualMessage);

        Map<String, List<String>> differences = new HashMap<>();
        List<String> notes = new ArrayList<>(); // missing keys
        List<String> nonMatchingKeys = new ArrayList<>(); // different keys - same value

        // get all keys to have all possible scenarios
        // Check for differences and missing keys in the expected map
        Set<String> allKeys = getAllKeys(expectedMap, actualMap);

        for (String key : allKeys) {
            String expectedValue = expectedMap.get(key);
            String actualValue = actualMap.get(key);

            if (actualValue == null) {

                for (String nestedKey : actualMap.keySet()) {

                    if (!nestedKey.equals(key) && expectedValue.equals(actualMap.get(nestedKey))) {

                        nonMatchingKeys.add(key + " : " + nestedKey + " = " + expectedValue);
                    }
                }

                notes.add("Missing key in Actual message: " + key);

            } else if (expectedValue == null) {

                for (String nestedKey : expectedMap.keySet()) {

                    if (!nestedKey.equals(key) && actualValue.equals(expectedMap.get(nestedKey))) {

                        nonMatchingKeys.add(key + " : " + nestedKey + " = " + actualValue);
                    }
                }

                notes.add("Missing key in Expected message: " + key);

            } else if (!expectedValue.equals(actualValue)) {

                differences.put(key, Arrays.asList(expectedValue, actualValue));
            }
        }

        differences.put("Notes", notes);
        differences.put("NonMatchingKeys", nonMatchingKeys);

        return differences;
    }

    public static Map<String, String> extractKeyValue(String message) {

        String[] lines = message.strip().split("\n");

        Map<String, String> map = new LinkedHashMap<>(); //keep order

        int n = 1;

        for (String line : lines) {

            String[] values = line.split("\\|");

            if (values.length >= 3) {

                if (values[1].strip().equals("Key") && values[2].strip().equals("Values")) {

                    continue;
                }

                if (values[1].strip().equals("")) {

                    values[1] = "*NULL-" + n;

                    n++;
                }

                map.put(values[1].strip(), values[2].strip());
            }
        }

        return map;
    }

    public static Set<String> getAllKeys(Map<String, String> expectedValue, Map<String, String> actualValue) {

        Set<String> allKeys = new LinkedHashSet<>(actualValue.keySet());
        allKeys.addAll(expectedValue.keySet());

        return allKeys;
    }

    public static void pushToExcel(Map<String,List<String>> diff) throws IOException {

        FileInputStream inputStream = new FileInputStream("/Users/delly/IdeaProjects/ExchangePlatform/src/test/java/testData/diffExchangeTable.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet worksheet = workbook.getSheet("Sheet1");
        XSSFRow currentRow = worksheet.createRow(0);
        currentRow.createCell(0).setCellValue("Key");
        currentRow.createCell(1).setCellValue("Expected Message");
        currentRow.createCell(2).setCellValue("Actual Message");
        currentRow.createCell(3).setCellValue("NonMatchingKeys");
        currentRow.createCell(4).setCellValue("Notes");

        int rowNum = 1;

        for (String key : diff.keySet()) {
                currentRow = worksheet.createRow(rowNum);
                currentRow.createCell(0).setCellValue(key);
                currentRow.createCell(1).setCellValue(diff.get(key).get(0));
                currentRow.createCell(2).setCellValue(diff.get(key).get(1));
                rowNum++;

        }

        inputStream.close();

        // save changes in Excel file
        try (FileOutputStream outputStream = new FileOutputStream("/Users/delly/IdeaProjects/ExchangePlatform/src/test/java/testData/diffExchangeTable.xlsx")) {
            workbook.write(outputStream);
        }

        workbook.close();




    }

}
