import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

public class ExcelEventReader {

    public static void main(String[] args) throws Exception {
        String excelFilePath = "path/to/your/excel/file.xlsx";
        OPCPackage opcPackage = OPCPackage.open(excelFilePath, PackageAccess.READ);
        XSSFReader reader = new XSSFReader(opcPackage);

        SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();
        StylesTable stylesTable = reader.getStylesTable();

        InputStream sheetInputStream = reader.getSheet("rId1");

        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        SheetHandler sheetHandler = new SheetHandler(sharedStringsTable, stylesTable);
        parser.setContentHandler(sheetHandler);

        InputSource sheetSource = new InputSource(sheetInputStream);
        parser.parse(sheetSource);

        sheetInputStream.close();
        opcPackage.close();
    }

    private static class SheetHandler extends DefaultHandler {

        private SharedStringsTable sharedStringsTable;
        private StylesTable stylesTable;
        private StringBuilder currentCellValue = new StringBuilder();
        private String lastContents;
        private boolean nextIsString;

        public SheetHandler(SharedStringsTable sharedStringsTable, StylesTable stylesTable) {
            this.sharedStringsTable = sharedStringsTable;
            this.stylesTable = stylesTable;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (name.equals("c")) {
                // Cell
                String cellType = attributes.getValue("t");
                nextIsString = cellType != null && cellType.equals("s");
                currentCellValue.setLength(0);
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
    }
}
