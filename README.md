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

		// Launch is mendatory
		rl.StartLaunch();

		// Test Suite can have only one instance per run
		rl.StartSuite("TestSuite", "Start of the Test Suite");

		// Execute BeforeTestSuite once test suite is constructed
		rl.StartBeforeSuite("BeforeTestSuite", "Unit Before Test Suite");
		rl.endBeforeSuite(Status.PASSED);

		// Construct Test Case
		rl.StartTest("TestStart", "Start of the Test Case");

		// Execute Before Test once Test case object is constructed
		rl.StartBeforeTest("BeforeTest", "Unit Before Test Test Execution");
		rl.endBeforeTest(Status.PASSED);

		rl.StartStep("TestStep 1", "Start of the Test Step 1");
		// Execute BeforeMethod once Test Step object is constructed
		rl.StartBeforeMethod("BeforeMethod", "Unit Before Test Test Execution");
		rl.endBeforeMethod(Status.PASSED);
		// Execute AfterMethod before Test Step object is ended
		rl.StartAfterMethod("AfterMethod", "Unit Before Test Test Execution");
		rl.endAfterMethod(Status.PASSED);
		rl.endStep(Status.PASSED);

		rl.StartStep("TestStep 2", "Start of the Test Step 2");
		rl.endStep(Status.FAILED);

		rl.StartStep("TestStep 3", "Start of the Test Step 3");
		rl.endStep(Status.SKIPPED);

		rl.StartStep("TestStep 4", "Start of the Test Step 4");
		rl.endStep(Status.PASSED);

		// Execute After Test before ending the Test case object
		rl.StartAfterTest("AfterTest", "Unit After Test Test Execution");
		rl.endAfterTest(Status.PASSED);

		// End Test Case
		rl.endTest(Status.FAILED);

		// Execute BeforeTestSuite before test suite is ended
		rl.StartAfterSuite("AfterTestSuite", "Unit After Test Suite");
		rl.endAfterSuite(Status.PASSED);

		rl.endSuite();

		rl.endLaunch();
		
	}
}
```