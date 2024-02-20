@Override
public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
    if ("c".equals(name)) {
        // Get the cell reference
        String cellRef = attributes.getValue("r");
        // Get the cell type
        String cellType = attributes.getValue("t");
        // Check if the cell is part of a merged region
        XSSFCellStyle cellStyle = styles.getStyle(Integer.parseInt(attributes.getValue("s")));
        if (cellStyle != null && cellStyle.getBorderTopEnum() == BorderStyle.NONE) {
            hasMergedCell = true;
        }
        // Print the cell reference
        System.out.println("Cell Reference: " + cellRef);
        // If the cell is a string type cell, retrieve the value from the shared strings table
        if ("s".equals(cellType)) {
            try {
                int idx = Integer.parseInt(attributes.getValue("s"));
                String cellValue = sharedStringsTable.getItemAt(idx).getString();
                System.out.println("Cell Value: " + cellValue);
            } catch (NumberFormatException e) {
                // Handle exception
            }
        }
    }
}
