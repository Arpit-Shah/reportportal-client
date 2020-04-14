# Report Portal Client Usage Example

## Add reportportal_configuration.xml file to location ./conf/

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<configuration>
  <reportportal_info>
    <property name="Project_Name">MOORTHIR_PERSONAL</property>
    <property name="TestSuite_Name">ARTOS_TESTSUITE_1</property>
    <property name="Release_Name">01.02.0002</property>
    <property name="Base_URL">http://192.168.1.23:8080</property>
    <property name="UUID">0abb483c-37f2-4abd-a029-3c7facfa7b7c</property>
  </reportportal_info>
</configuration>

```

```java
public class Test {

	public static void main(String[] args) {
		ReportPortalLauncher rl = new ReportPortalLauncher();

		// Launch is mandatory
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
		// log something
		rl.log(LogStatus.Debug, "This is log line 1");
		rl.log(LogStatus.Debug, "This is log line 2");
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