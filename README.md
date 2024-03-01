import java.util.HashMap;
import java.util.Map;

private static class SheetHandler extends DefaultHandler {
    private final SharedStringsTable sst;
    private boolean nextIsString;
    private StringBuilder lastContents = new StringBuilder();
    private boolean inMergeCell;
    private boolean isMerged;
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

    @Override
    public void endRow(int rowNum) throws SAXException {
        if (isMerged) {
            if (!mergedCellsAtStartOfRow.containsKey(rowNum)) {
                mergedCellsAtStartOfRow.put(rowNum, 0);
            }
            mergedCellsAtStartOfRow.put(rowNum, mergedCellsAtStartOfRow.get(rowNum) + 1);
        }
        isMerged = false;
    }

    public Map<Integer, Integer> getMergedCellsAtStartOfRow() {
        return mergedCellsAtStartOfRow;
    }
}
