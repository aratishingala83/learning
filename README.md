============
==============
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExcelEventBasedReader {

    public static void main(String[] args) throws IOException, InvalidFormatException, ParserConfigurationException, SAXException {
        File inputFile = new File("path/to/your/large_excel_file.xlsx");

        // Count variable to store the number of records
        int recordCount = 0;

        // Event API code starts here
        OPCPackage opcPackage = OPCPackage.open(inputFile);
        XSSFReader xssfReader = new XSSFReader(opcPackage);
        SharedStringsTable sharedStringsTable = xssfReader.getSharedStringsTable();
        StylesTable styles = xssfReader.getStylesTable();

        InputStream sheetInputStream = xssfReader.getSheet("rId1");
        XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

        ContentHandler handler = new DefaultHandler() {
            private boolean inRow = false;
            private boolean hasMergedCell = false;

            @Override
            public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
                if ("row".equals(name)) {
                    inRow = true;
                    hasMergedCell = false;
                } else if (inRow && "c".equals(name)) {
                    // Check if the cell is part of a merged region
                    String cellRef = attributes.getValue("r");
                    if (cellRef != null) {
                        XSSFCellStyle cellStyle = styles.getStyle(Integer.parseInt(attributes.getValue("s")));
                        if (cellStyle != null && cellStyle.getBorderTopEnum() == BorderStyle.NONE) {
                            hasMergedCell = true;
                        }
                    }
                }
            }

            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {
                if ("row".equals(name)) {
                    // Increment the record count only if the row doesn't have merged cells
                    if (!hasMergedCell) {
                        recordCount++;
                    }
                    inRow = false;
                }
            }
        };

        parser.setContentHandler(handler);
        parser.parse(new InputSource(sheetInputStream));

        // Output the record count
        System.out.println("Number of records: " + recordCount);

        // Close resources
        sheetInputStream.close();
        opcPackage.close();
    }
}
