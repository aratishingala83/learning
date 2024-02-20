import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ReadLargeExcelFileWithSAX {
    public static void main(String[] args) {
        String excelFilePath = "your_excel_file.xlsx";

        try (OPCPackage pkg = OPCPackage.open(new FileInputStream(new File(excelFilePath)))) {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
            StylesTable stylesTable = reader.getStylesTable();

            XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            ContentHandler handler = new SheetHandler(sharedStringsTable, stylesTable);
            parser.setContentHandler(handler);

            Iterator<InputStream> sheets = reader.getSheetsData();
            while (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    InputSource sheetSource = new InputSource(sheet);
                    parser.parse(sheetSource);
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sharedStringsTable;
        private StylesTable stylesTable;
        private String lastContents;
        private boolean nextIsString;

        private SheetHandler(SharedStringsTable sharedStringsTable, StylesTable stylesTable) {
            this.sharedStringsTable = sharedStringsTable;
            this.stylesTable = stylesTable;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (name.equals("c")) {
                String cellType = attributes.getValue("t");
                nextIsString = cellType != null && cellType.equals("s");
            }
            lastContents = "";
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx)).toString();
                nextIsString = false;
            }
            System.out.print(lastContents + "\t");
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }
    }
}
