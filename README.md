import org.apache.poi.ss.usermodel.*;

public class CheckMergedCellsInRow {


public static boolean rowHasMergedCells(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            int lastCellNum = row.getLastCellNum();
            for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
                for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                    CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                    if (mergedRegion.isInRange(rowIndex, colIndex)) {
                        // Check if the merged region intersects with the current cell
                        return true;
                    }
                }
            }
        }
        return false;
    }

    
    public static boolean rowHasMergedCells(Sheet sheet, int rowIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(rowIndex, mergedRegion.getFirstColumn())) {
                // Check if the merged region intersects with the given row
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // Example usage:
        Workbook workbook = ... // Initialize your workbook (e.g., HSSFWorkbook or XSSFWorkbook)
        Sheet sheet = workbook.getSheetAt(0); // Get the desired sheet

        int targetRow = 1; // Row index to check for merged cells

        boolean hasMergedCells = rowHasMergedCells(sheet, targetRow);
        if (hasMergedCells) {
            System.out.println("Row " + targetRow + " contains merged cells.");
        } else {
            System.out.println("Row " + targetRow + " does not contain merged cells.");
        }
    }
}
