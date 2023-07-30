import java.util.logging.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.chrome.ChromeDriver; 
import java.io.IOException;
import java.util.Random;

public class AQLTest {

    private static final Logger logger = Logger.getLogger(AQLTest.class.getName());
    private static final String CHROME_DRIVER_ADDRESS = System.getProperty("user.dir")+"\\AQLTest\\webdriver\\chromedriver.exe";
    private static final String QIMA_TARGET_URL = "https://www.qima.com/aql-acceptable-quality-limit";
    private static final String STRING_EMPTY = "";
    private static final int LIMIT_RADOM_BOUND = 99999990;

    public static void main(String[] args) {
        // Set up the WebDriver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_ADDRESS);      
        ExecutionReport report = new ExecutionReport("AQL TEST");
        WebDriver driver = null;
        String browserIDHandler = "";
              
        // Repeat Steps 1 - 15 five times
        for (int i = 0; i < 5; i++) {
            try {
                
                // Create driver
                driver = new ChromeDriver();
                driver.manage().window().maximize();
                browserIDHandler = driver.getWindowHandle();
                logger.info("Browser window ID: " + browserIDHandler);

                // Open the page
                driver.get(QIMA_TARGET_URL);
                logger.info("Navigation URL: " + driver.getCurrentUrl());

                //Accept cookie
                if (driver.findElement(By.id("CybotCookiebotDialogBodyButtonAccept")).isDisplayed())
                    driver.findElement(By.id("CybotCookiebotDialogBodyButtonAccept")).click();
                //Click on "NO" option to NOT redirect site to local pt-br
                if (driver.findElement(By.className("button-no")).isDisplayed())
                    driver.findElement(By.className("button-no")).click();

                logger.info("--------------------ROUND ["+ (i+1) +"]--------------------");
                report.startReport("--------------------ROUND ["+ (i+1) +"]--------------------");
                // Enter a random positive integer QUANTITY (>=2)
                Random random = new Random();
                int quantity = random.nextInt(LIMIT_RADOM_BOUND) + 2;
                
                driver.findElement(By.name("aql-calculator-quantity")).sendKeys(String.valueOf(quantity));
                logger.info("Entering quantity: " + quantity);

                // Select a random on FULL INSPECTION LEVEL
                //String[] inspectionLevels = {"I", "II", "III","S1", "S2", "S3","S4"};
                // Select a random on NORMAL INSPECTION LEVEL (TableB applys to normal inspection)
                String[] inspectionLevels = {"I", "II", "III"};
                int inspectionLevelIndex = random.nextInt(inspectionLevels.length);
                String inspectionLevel = inspectionLevels[inspectionLevelIndex];
                            
                Select sInspaection = new Select( driver.findElement(By.name("aql-calculator-inspection-level")));
                sInspaection.selectByValue(inspectionLevel);
                logger.info("Selecting inspection level: " + inspectionLevel);


                // Select a random CRITICAL DEFECTS AQL
                //PS.: The selection is by .value so 'Not Allowed' = 0
                String[] criticalDefectsAQLs = 
                {"0","0.065","0.10", "0.15", "0.25", "0.40","0.65","1.0","1.5","2.5","4.0","6.5"};
                int criticalDefectsAQLIndex = random.nextInt(criticalDefectsAQLs.length);
                String criticalDefectsAQL = criticalDefectsAQLs[criticalDefectsAQLIndex];
                            
                Select sCriticalDefect = new Select( driver.findElement(By.name("aql-calculator-critical-aql")));
                sCriticalDefect.selectByValue(criticalDefectsAQL);
                logger.info("Selecting critical defects AQL: " + criticalDefectsAQL);

                // Select a random MAJOR DEFECTS AQL
                //PS.: The selection is by .value so 'Not Allowed' = 0
                String[] majorDefectsAQLs = 
                {"0","0.065","0.10", "0.15", "0.25", "0.40","0.65","1.0","1.5","2.5","4.0","6.5"};
                int majorDefectsAQLIndex = random.nextInt(majorDefectsAQLs.length);
                String majorDefectsAQL = majorDefectsAQLs[majorDefectsAQLIndex];
                
                Select sMajorDefect = new Select( driver.findElement(By.name("aql-calculator-major-aql")));
                sMajorDefect.selectByValue(majorDefectsAQL);
                logger.info("Selecting major defects AQL: " + majorDefectsAQL);

                // Select a random MINOR DEFECTS AQL
                //PS.: The selection is by .value so 'Not Allowed' = 0
                String[] minorDefectsAQLs = 
                {"0","0.065","0.10", "0.15", "0.25", "0.40","0.65","1.0","1.5","2.5","4.0","6.5"};
                int minorDefectsAQLIndex = random.nextInt(minorDefectsAQLs.length);
                String minorDefectsAQL = minorDefectsAQLs[minorDefectsAQLIndex];
                
                Select sMinorDefect = new Select( driver.findElement(By.name("aql-calculator-minor-aql")));
                sMinorDefect.selectByValue(minorDefectsAQL);
                logger.info("Selecting minor defects AQL: " + minorDefectsAQL);
                

                // Verify that CRITICAL Sample Size value is correct or not
                validations ("CRITICAL",driver, report, quantity, inspectionLevel, criticalDefectsAQL);

                // Verify that MAJOR Sample Size value is correct or not
                validations ("MAJOR",driver, report, quantity, inspectionLevel, majorDefectsAQL);

                // Verify that MINOR Sample Size value is correct or not
                validations ("MINOR",driver, report, quantity, inspectionLevel, minorDefectsAQL);

                // Close the WebDriver
                driver.quit();
                logger.info("WebDriver closed");
            } catch (Exception e) {
                
                if (!browserIDHandler.equals(STRING_EMPTY) & (driver != null)){
                    driver.quit();
                    logger.info("WebDriver closed by its error (resume next Round)");
                }
                report.addError("xxxxx- ROUND ERROR (resume next) -xxxxx");
            }
        }
        
        
        // End and the opens the report
        report.finishReport();
        report.openReport();
    }

    public static void validations(String ACLtype, WebDriver driver, ExecutionReport report, int quantity, String inspectionLevel, String defectsAQL){

        // Actual (on screen)
        String actualSampleSize = driver.findElement(By.id(ACLtype.toLowerCase()+"-sample-size")).getText().replace(",", "");
        String actualAcceptPoint = driver.findElement(By.id(ACLtype.toLowerCase()+"-accept-point")).getText();
        String actualRejectPoint = driver.findElement(By.id(ACLtype.toLowerCase()+"-reject-point")).getText();

        logger.info("Actual "+ACLtype.toLowerCase()+" sample size:  " + actualSampleSize);
        logger.info("Actual "+ACLtype.toLowerCase()+" accept point: " + actualAcceptPoint);
        logger.info("Actual "+ACLtype.toLowerCase()+" reject point: " + actualRejectPoint);

        // Expected (calculated based on Excel tables in the project, not in the image on website)
        String [] expectedResult = {"","","",""};
        Evaluate expectedvalue = new Evaluate(quantity, inspectionLevel, defectsAQL);
        
        try {
            expectedResult = expectedvalue.findExpectedResultValues();
        } catch (EncryptedDocumentException e) {
            logger.severe("Error on attempt to generate Expected reults (encripted)" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.severe("Error on attempt to generate Expected reults (IO)" + e.getMessage());
            e.printStackTrace();
        }
        
        logger.info(
            "\n"+ACLtype.toUpperCase()+" VALIDATION\n" +
            "Considering the search criterias (random input):\n" +
            "     [1] - Quantity/Lot size = " + quantity + "\n" +
            "     [2] - Inspection Level  = " + inspectionLevel +"\n" + 
            "     [3] - AQL Level         = " + defectsAQL +"\n\n" + 
    
            "We calculate the following Results (expected output): \n" + 
            "     [Table A] - Code Letter   = '" + expectedResult[3] + "'\n" +
            "     [Table B] - Sample Size  = "  + expectedResult[0] + "\n" +
            "     [Table B] - Accept Point = "  + expectedResult[1] + "\n" +
            "     [Table B] - Reject Point = "  + expectedResult[2] + "\n");
        
        //Validations Actual x Expected
        report.addInformation("["+ACLtype.toUpperCase()+"] - Considering the search criterias (random input):\n <br>" +
                            "     [1] - Quantity/Lot size = " + quantity + "\n <br>" +
                            "     [2] - Inspection Level  = " + inspectionLevel +"\n <br>" + 
                            "     [3] - AQL Level         = " + defectsAQL +"\n\n <br>" +
                            "     [FOUND] - Found Code Letter   = '" + expectedResult[3]+"'");
        //Sample Size
        report.addValidation("["+ACLtype.toUpperCase()+"] - Sample Size: <br>", 
                                expectedResult[0], 
                                actualSampleSize, 
                                expectedResult[0].equals(actualSampleSize));
        //Accept Point
        report.addValidation("["+ACLtype.toUpperCase()+"] - Accept Point: <br>", 
                                expectedResult[1], 
                                actualAcceptPoint, 
                                expectedResult[1].equals(actualAcceptPoint));
        //Reject Point
        report.addValidation("["+ACLtype.toUpperCase()+"] - Reject Point: <br>", 
                                expectedResult[2], 
                                actualRejectPoint, 
                                expectedResult[2].equals(actualRejectPoint));

    }
}
