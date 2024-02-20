import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class ExcelEventReader {

    public static void main(String[] args) throws Exception {
        String excelFilePath = "path/to/your/excel/file.xlsx";
        InputStream inputStream = new FileInputStream(excelFilePath);
        XSSFReader reader = new XSSFReader(OPCPackage.open(inputStream));

        SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
        StylesTable stylesTable = reader.getStylesTable();

        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        SheetHandler sheetHandler = new SheetHandler(sharedStringsTable, stylesTable, reader.getSheet("rId1"));
        parser.setContentHandler(sheetHandler);

        InputStream sheetInputStream = reader.getSheet("rId1");
        InputSource sheetSource = new InputSource(sheetInputStream);
        parser.parse(sheetSource);

        sheetInputStream.close();
        inputStream.close();
    }

    private static class SheetHandler extends DefaultHandler {

        private SharedStringsTable sharedStringsTable;
        private StylesTable stylesTable;
        private Sheet sheet;
        private StringBuffer currentCellValue = new StringBuffer();
        private String lastContents;
        private boolean nextIsString;

        public SheetHandler(SharedStringsTable sharedStringsTable, StylesTable stylesTable, Sheet sheet) {
            this.sharedStringsTable = sharedStringsTable;
            this.stylesTable = stylesTable;
            this.sheet = sheet;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (name.equals("c")) {
                // Cell
                String cellType = attributes.getValue("t");
                nextIsString = cellType != null && cellType.equals("s");
                currentCellValue.setLength(0);

                // Get row and column indices of the current cell
                int colIdx = CellReference.convertColStringToIndex(attributes.getValue("r").replaceAll("[0-9]", ""));
                int rowIdx = Integer.parseInt(attributes.getValue("r").replaceAll("[^0-9]", "")) - 1;

                // Check if the current cell is merged
                if (isCellMerged(rowIdx, colIdx)) {
                    System.out.println("This cell is part of a merged region.");
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                currentCellValue.append(sharedStringsTable.getItemAt(idx).toString());
                nextIsString = false;
            }

            if (name.equals("v")) {
                // Value of cell
                System.out.println(currentCellValue.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

        private boolean isCellMerged(int rowIdx, int colIdx) {
            List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
            for (CellRangeAddress mergedRegion : mergedRegions) {
                if (rowIdx >= mergedRegion.getFirstRow() && rowIdx <= mergedRegion.getLastRow() &&
                        colIdx >= mergedRegion.getFirstColumn() && colIdx <= mergedRegion.getLastColumn()) {
                    return true;
                }
            }
            return false;
        }
    }
}
