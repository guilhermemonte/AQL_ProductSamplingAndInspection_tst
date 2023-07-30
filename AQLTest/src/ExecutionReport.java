import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.net.URI;


public class ExecutionReport {
    private ExtentReports extent;
    private ExtentTest test; 
    private String filePathName;

    public ExecutionReport(String tstName) {

         // set timestamp to add on file name                
        long timestampMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDD_HHmmss");
        String timestamp = sdf.format(timestampMillis);
        // Initialize ExtentReports with a path to store the report
        this.filePathName = System.getProperty("user.dir")+"\\AQLTest\\outputReport\\AQLtstReport"+timestamp+".html";
        ExtentHtmlReporter htmlReporter = 
            new ExtentHtmlReporter(this.filePathName);
        
        this.extent = new ExtentReports();
        this.extent.attachReporter(htmlReporter);
        this.test = this.extent.createTest(tstName);
    }

    @BeforeSuite
    public void startReport(String roundID) {
        // Add test result to ExtentReports
        this.test.info(roundID);
    }

    @Test
    public void addValidation(String valName, String expected, String actual, Boolean successYN) {

        if (successYN) {
            this.test.log(Status.PASS, valName + " ACTUAL--> " + actual + " EQUALS " + expected + " <--EXPECTED");
        } else {
            this.test.log(Status.FAIL, valName + " ACTUAL--> " + actual + " NOT equals " + expected + " <--EXPECTED");
        }
    }

    @Test
    public void addInformation(String info) {
        // Add test info
        this.test.info(info);
    }
    @Test
    public void addError(String error){
        // Add test error
        this.test.log(Status.FAIL, error);

    }
    public void openReport() {
        // Get the path to the HTML file
        
        String path = this.filePathName;
        File file = new File(path);
        URI uri = file.toURI();
        try {
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            System.out.println("Issue to open the Report file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterSuite
    public void finishReport() {
        // Flush the ExtentReports and close the report
        this.extent.flush();
    }
}
