import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
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

        // Event API code starts here
        OPCPackage opcPackage = OPCPackage.open(inputFile);
        XSSFReader xssfReader = new XSSFReader(opcPackage);
        SharedStringsTable sharedStringsTable = xssfReader.getSharedStringsTable();
        StylesTable styles = xssfReader.getStylesTable();

        InputStream sheetInputStream = xssfReader.getSheet("rId1");
        XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();

        DefaultHandler handler = new DefaultHandler() {
            private boolean inRow = false;
            private boolean hasValue = false;

            @Override
            public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
                if ("row".equals(name)) {
                    inRow = true;
                    hasValue = false;
                } else if (inRow && "c".equals(name)) {
                   // Check if the cell has content
                    String cellType = attributes.getValue("t");
                    String cellRef = attributes.getValue("r");
                    System.out.print("Cell Reference: " + cellRef + ", ");
                    if ("s".equals(cellType)) {
                        // String cell
                        int idx = Integer.parseInt(attributes.getValue("s"));
                        String cellValue = sharedStringsTable.getItemAt(idx).getString();
                        System.out.println("Cell Value: " + cellValue);
                    } else if ("inlineStr".equals(cellType)) {
                        // Inline string cell
                        System.out.println("Inline String Cell Value: " + attributes.getValue("t"));
                    } else if ("n".equals(cellType)) {
                        // Numeric cell
                        System.out.println("Numeric Cell Value: " + attributes.getValue("v"));
                    } else if ("b".equals(cellType)) {
                        // Boolean cell
                        System.out.println("Boolean Cell Value: " + attributes.getValue("v"));
                    }
                }
            }

            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {
                if ("row".equals(name)) {
                    // Output the result for the row
                    System.out.println("Row has value in all columns: " + hasValue);
                    inRow = false;
                }
            }
        };

        parser.setContentHandler(handler);
        parser.parse(new InputSource(sheetInputStream));

        // Close resources
        sheetInputStream.close();
        opcPackage.close();
    }
}
