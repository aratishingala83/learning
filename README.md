import java.util.HashMap;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

private static class SheetHandler extends DefaultHandler {
    private final SharedStringsTable sst;
    private boolean nextIsString;
    private StringBuilder lastContents = new StringBuilder();
    private boolean inMergeCell;
    private boolean isMerged;
    private int currentRow;
    private int mergedCellsAtStartOfRowCount;
    private Map<Integer, Integer> mergedCellsAtStartOfRow = new HashMap<>();

    private SheetHandler(SharedStringsTable sst) {
        this.sst = sst;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (name.equals("c")) {
            String cellType = attributes.getValue("t");
            nextIsString = cellType != null && cellType.equals("s");
        } else if (name.equals("mergeCell")) {
            inMergeCell = true;
            isMerged = true;
            if (currentRow > 0) {
                mergedCellsAtStartOfRow.put(currentRow, mergedCellsAtStartOfRowCount);
            }
            mergedCellsAtStartOfRowCount++;
        } else if (name.equals("row")) {
            if (currentRow > 0) {
                mergedCellsAtStartOfRow.put(currentRow, mergedCellsAtStartOfRowCount);
            }
            currentRow++;
            mergedCellsAtStartOfRowCount = 0;
        } else {
            inMergeCell = false;
        }
        lastContents.setLength(0);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (nextIsString) {
            int idx = Integer.parseInt(lastContents.toString());
            lastContents.setLength(0);
            nextIsString = false;
            XSSFRichTextString v = new XSSFRichTextString(sst.getEntryAt(idx));
            System.out.println("String Cell Value: " + v.toString() + ", Merged: " + isMerged);
        } else if (name.equals("v")) {
            System.out.println("Numeric Cell Value: " + lastContents.toString() + ", Merged: " + isMerged);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        lastContents.append(ch, start, length);
    }

    public Map<Integer, Integer> getMergedCellsAtStartOfRow() {
        return mergedCellsAtStartOfRow;
    }
}
