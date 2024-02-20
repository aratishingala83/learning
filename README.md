
---
---
---
----

-----




public class LargeExcelReader {
    public static void main(String[] args) {
        String excelFilePath = "path/to/your/large/file.xlsx";

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new SXSSFWorkbook(new XSSFWorkbook(fis))) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            // Get the merged regions
            List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();

            for (Row row : sheet) {
                for (Cell cell : row) {
                    // Check if the cell is merged
                    boolean isMerged = isCellMerged(mergedRegions, cell);
                    if (isMerged) {
                        System.out.print("Merged\t");
                    } else {
                        System.out.print("Not Merged\t");
                    }

                    // Process each cell as needed
                    CellType cellType = cell.getCellType();
                    if (cellType == CellType.STRING) {
                        System.out.print(cell.getStringCellValue() + "\t");
                    } else if (cellType == CellType.NUMERIC) {
                        System.out.print(cell.getNumericCellValue() + "\t");
                    } else if (cellType == CellType.BOOLEAN) {
                        System.out.print(cell.getBooleanCellValue() + "\t");
                    } // Handle other cell types as needed
                }
                System.out.println(); // Move to the next row
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isCellMerged(List<CellRangeAddress> mergedRegions, Cell cell) {
        for (CellRangeAddress mergedRegion : mergedRegions) {
            if (mergedRegion.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return true;
            }
        }
        return false;
    }
}
