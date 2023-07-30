import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row; 
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Evaluate {
    private int _quantity;
    private String _inspectionLevel;
    private String _defectsAQL;

    private static final String DATATABLES_FILE_ADDRESS = System.getProperty("user.dir")+"\\AQLTest\\inputData\\DataTables.xlsx";
    private static final String DATATABLES_TABLE_A_NAME = "TableA";
    private static final String DATATABLES_TABLE_B_NAME = "TableB";

    private Evaluate() {
        this._quantity = 0;
        this._inspectionLevel = null;
        this._defectsAQL = null;
    }
    public Evaluate(int _quantity, String _inspectionLevel, String _defectsAQL) {
        this._quantity = _quantity;
        this._inspectionLevel = _inspectionLevel;
        this._defectsAQL = _defectsAQL;
    }

    private int get_quantity() {
        return _quantity;
    }
    private void set_quantity(int _quantity) {
        this._quantity = _quantity;
    }
    private String get_inspectionLevel() {
        return _inspectionLevel;
    }
    private void set_inspectionLevel(String _inspectionLevel) {
        this._inspectionLevel = _inspectionLevel;
    }
    private String get_defectsAQL() {
        return _defectsAQL;
    }
    private void set_defectsAQL(String _defectsAQL) {
        this._defectsAQL = _defectsAQL;
    }

    public static void main (String[] args) throws EncryptedDocumentException, IOException{
        
        String [] criticalExpResults = {"","","",""};
        Evaluate evl = new Evaluate();

        evl.set_quantity(1000);
        evl.set_inspectionLevel("II");
        evl.set_defectsAQL("0");


        try {
            criticalExpResults = evl.findExpectedResultValues();
        } catch (EncryptedDocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(
                "\n\n" +
                "Considering the search criterias (random input):\n" +
                "     [1] - Quantity/Lot size = " + evl.get_quantity() + "\n" +
                "     [2] - Inspection Level  = " + evl.get_inspectionLevel() +"\n" + 
                "     [3] - AQL Level         = " + evl.get_defectsAQL() +"\n\n" + 
        
                "We calculate the following Results (expected output): \n" + 
                "     [Table A] - Code Leter   = '" + criticalExpResults[3] + "'\n" +
                "     [Table B] - Sample Size  = "  + criticalExpResults[0] + "\n" +
                "     [Table B] - Accept Point = "  + criticalExpResults[1] + "\n" +
                "     [Table B] - Reject Point = "  + criticalExpResults[2] + "\n");

    }
    
    //Functions
    private String findCodeLetter()throws EncryptedDocumentException, IOException{

        String codeLetter = "";
        int rowIndexA = -1;
        int colunmIdexA = -1;

        // Create or manipulate an Excel table
        String filePath = DATATABLES_FILE_ADDRESS;
        String tableA = DATATABLES_TABLE_A_NAME; 

        //Table A search
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileIn);
            Sheet sheet = workbook.getSheet(tableA);
            
            // Iterate through rows and cells to read data
            if (sheet != null) {

                // Get the last row index (0-based)
                int lastRowNum = sheet.getLastRowNum();

                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        // Get the last cell index (0-based)
                        int lastCellNum = row.getLastCellNum();

                        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                            Cell cell = row.getCell(cellIndex);
                            if (cell != null) {

                                // Process the cell data based on its type
                                CellType cellType = cell.getCellType();
                                switch (cellType) {
                                    case STRING:
                                        String cellValue = cell.getStringCellValue();
                                        //find column (inspection Level)
                                        if (cellValue.equals(get_inspectionLevel())){
                                            colunmIdexA = cell.getColumnIndex();
                                        }
                                        break;
                                    case NUMERIC:
                                        double numericValue = cell.getNumericCellValue();
                                        //find range row index
                                        if(get_quantity() <= numericValue){
                                                rowIndexA = cellIndex;
                                        }
                                        break;
                                    // Handle other cell types as needed (e.g., BOOLEAN, FORMULA, etc.)
                                    default:
                                        //Todo...
                                }
                            }

                            if(rowIndexA > -1 & colunmIdexA > -1){
                                codeLetter = row.getCell(colunmIdexA).toString();
                                break;
                            }
                        }
                        //Flag that indicates the search id over
                        if(rowIndexA > -1 & colunmIdexA > -1){
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Sheet '" + tableA + "' not found!");
            }
            //Stop workbook and file
            workbook.close();
            fileIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return codeLetter;
    }

    public String[] findExpectedResultValues()throws EncryptedDocumentException, IOException{
        String [] findExpectedResultValues = {"","","",""};
        String codeLetter = "";
        String searchAC = "";
        String searchRE = "";

        int rowIndexB = -1;
        int columnSampleSize = -1;
        int columnIndexAC = -1;
        int columnIndexRE = -1;

        // Create or manipulate an Excel table
        String filePath = DATATABLES_FILE_ADDRESS;
        String tableB = DATATABLES_TABLE_B_NAME; 

        codeLetter = findCodeLetter();

        /**************************************************************************
        |Setting search criteria based on TableB colunm name structure, examples: |
        |     [1] - AQL defect level 0.065, there are colunms: 0.065AC & 0.065RE  |
        |     [2] - AQL defect level 0.25, there are colunms: 0.25AC & 0.25RE     |
        **************************************************************************/
        searchAC = get_defectsAQL().toString() + "AC";
        searchRE = get_defectsAQL().toString() + "RE";

        //Table B search
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileIn);
            Sheet sheet = workbook.getSheet(tableB);
            
            // Iterate through rows and cells to read data
            if (sheet != null) {

                // Get the last row index (0-based)
                int lastRowNum = sheet.getLastRowNum();

                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        // Get the last cell index (0-based)
                        int lastCellNum = row.getLastCellNum();

                        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                            Cell cell = row.getCell(cellIndex);
                            if (cell != null) {

                                // Process the cell data based on its type
                                CellType cellType = cell.getCellType();
                                switch (cellType) {
                                    case STRING:
                                        String cellValue = cell.getStringCellValue();
                                        //find colunm Accept Point (AC)
                                        if (cellValue.equals(searchAC)){
                                            columnIndexAC = cell.getColumnIndex();
                                        } //find colunm Reject Point (RE)
                                        else if (cellValue.equals(searchRE)){
                                            columnIndexRE = cell.getColumnIndex();
                                        } //find Code letter row
                                        else if (cellValue.equals(codeLetter)){
                                            rowIndexB = cell.getRowIndex();
                                        }
                                        break;
                                    case NUMERIC:
                                        double numericValue = cell.getNumericCellValue();
                                        //
                                        if (rowIndexB > -1 & numericValue > -1){
                                            columnSampleSize = cell.getColumnIndex();
                                        }
                                        break;
                                    // Handle other cell types as needed (e.g., BOOLEAN, FORMULA, etc.)
                                    default:
                                        //todo...
                                }
                            } 
                            //When find all the indexes reads values from the Excel data table
                            if(columnIndexAC > -1 & columnIndexRE > -1 & rowIndexB > -1 & columnSampleSize > -1){

                                Row rowResult = sheet.getRow(rowIndexB);

                                findExpectedResultValues[0] = 
                                    String.valueOf((int) rowResult.getCell(columnSampleSize).getNumericCellValue());

                                findExpectedResultValues[1] = 
                                    String.valueOf((int) rowResult.getCell(columnIndexAC).getNumericCellValue());

                                findExpectedResultValues[2] = 
                                    String.valueOf((int) rowResult.getCell(columnIndexRE).getNumericCellValue());

                                findExpectedResultValues[3] =codeLetter;
                                /*//Output test block
                                System.out.println("\n "+
                                    "For the Code Letter = " + findExpectedResultValues[3] + "\n" +
                                    "    Sample size :" + findExpectedResultValues[0] + "\n" +
                                    "    Accept Point:" + findExpectedResultValues[1] + "\n" +
                                    "    Reject Point:" + findExpectedResultValues[2] + "\n"); 
                                */
                                break;
                            }
                        }
                        //Double check Flag that indicates the search id over
                        if(columnIndexAC > -1 & columnIndexRE > -1 & rowIndexB > -1 & columnSampleSize > -1){
                            break;
                        }
                        
                    }
                }
            } else {
                System.out.println("Sheet '" + tableB + "' not found!");
            }
            //Stop workbook
            workbook.close();
            fileIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return findExpectedResultValues;
    }
}
