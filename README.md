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
