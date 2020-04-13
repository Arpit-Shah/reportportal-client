# reportportal-client

```java
public class Test {

	public static void main(String[] args) {
		String launchName = "Trial Test";
		String projectName = "REPORTPORTAL_PROJECT_NAME";
		String releaseName = "01.01.0001";
		String uUID = "0dfb483c-3342-4abd-c02a-3c7fac017b7a";
		String sURL = "http://58.22.3.234:8080";
		ReportPortalLauncher rl = new ReportPortalLauncher(launchName, projectName, releaseName, uUID, sURL);

		rl.StartLaunch();

		rl.StartBeforeSuite("BeforeTestSuite", "Unit Before Test Suite");
		rl.endBeforeSuite(Status.PASSED);
		
		rl.StartSuite("TestSuite", "Start of the Test Suite");

		rl.StartBeforeTest("BeforeTest", "Unit Before Test Test Execution");
		rl.endBeforeTest(Status.PASSED);
		
		rl.StartTest("TestStart", "Start of the Test Case");
		
		rl.StartStep("TestStep 1", "Start of the Test Step 1");
		rl.endStep(Status.PASSED);
		rl.StartStep("TestStep 2", "Start of the Test Step 2");
		rl.endStep(Status.Failed);
		rl.StartStep("TestStep 3", "Start of the Test Step 3");
		rl.endStep(Status.SKIPPED);
		rl.StartStep("TestStep 4", "Start of the Test Step 4");
		rl.endStep(Status.PASSED);
		
		rl.endTest(Status.Failed);
		
		rl.StartAfterTest("AfterTest", "Unit After Test Test Execution");
		rl.endAfterTest(Status.PASSED);
		
		rl.endSuite();
		
		rl.StartAfterSuite("AfterTestSuite", "Unit After Test Suite");
		rl.endAfterSuite(Status.PASSED);
		
		rl.endLaunch();
		
	}
}
```