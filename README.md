import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;

public class CustomRowHandler extends DefaultHandler {

    private boolean isInRow;
    private int rowNumber;
    private int dataRecordCount;

    // Define the header row and column count
    private int headerRowCount = 1; // Assuming the header row is at index 1
    private int columnHeaderCount = 1; // Assuming the column headers are at index 1

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if ("row".equals(name)) {
            rowNumber++;
            isInRow = true;

            // Skip processing header rows and column header row
            if (rowNumber <= headerRowCount + columnHeaderCount) {
                isInRow = false;
            }
        } else if (isInRow && "c".equals(name)) {
            // Assuming that column names are represented as letters (e.g., A, B, C, ...)
            String cellRef = attributes.getValue("r");
            String columnName = cellRef.replaceAll("[0-9]", "");

            // Skip processing column header cells
            if (rowNumber == headerRowCount + 1) {
                isInRow = false;
                return;
            }

            // Implement your logic to process data cells
            // Here, you can count the number of non-empty cells as data records
            // Example: check for non-empty cell value
            // You can also implement more specific conditions based on your data
            // For example, if you have specific columns that must have data, you can check those columns
            // Example: check specific column indexes/column names for data
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if ("row".equals(name)) {
            isInRow = false;
        }
    }

    // Example usage
    public static void main(String[] args) throws Exception {
        OPCPackage pkg = OPCPackage.open("your_excel_file.xlsx", PackageAccess.READ);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        CustomRowHandler handler = new CustomRowHandler();
        parser.setContentHandler(handler);
        InputStream sheet = r.getSheet("rId1");
        parser.parse(new InputSource(sheet));
        sheet.close();

        System.out.println("Data record count: " + handler.getDataRecordCount());
    }

    public int getDataRecordCount() {
        return dataRecordCount;
    }
}


































































































import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventDrivenExcelReader {

    public static void main(String[] args) throws OpenXML4JException, ParserConfigurationException, SAXException, IOException {
        try (OPCPackage pkg = OPCPackage.open("example.xlsx")) {
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = r.getSharedStringsTable();
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) r.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream sheetStream = sheets.next()) {
                    String sheetName = sheets.getSheetName();
                    System.out.println("Sheet name: " + sheetName);

                    InputSource sheetSource = new InputSource(sheetStream);
                    try {
                        parser.setContentHandler(new SheetHandler(sst));
                        parser.parse(sheetSource);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
        private List<String> row = new ArrayList<>();
        private int currentRow = -1;
        private int currentCol = -1;

        public SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (name.equals("c")) {
                String cellType = attributes.getValue("t");
                if (cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            } else if (name.equals("row")) {
                currentRow = Integer.parseInt(attributes.getValue("r")) - 1;
                row.clear();
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            if (name.equals("c")) {
                if (currentCol > -1) {
                    int idx = Integer.parseInt(lastContents);
                    if (nextIsString) {
                        String str = sst.getItemAt(idx).getString();
                        row.add(str);
                    } else {
                        row.add(lastContents);
                    }
                }
                currentCol++;
            } else if (name.equals("row")) {
                // Process row data here
                System.out.println("Row " + (currentRow + 1) + ": " + row);
                currentCol = -1;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents = new String(ch, start, length);
        }
    }
}
