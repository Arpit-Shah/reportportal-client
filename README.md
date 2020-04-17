# README #

This project is distributed under MIT License. Please read "LICENSE.md" file for more details.

# Report Portal Client
reportportal-client provides an API that can be used by anyone who would like to integrate with "https://reportportal.io/". 
It currently supports all methods required to support "https://www.theartos.com/" integration but it can be further extended to support other frameworks if requested. 

# Recommended Artos version is 0.0.13 or above
| Release     |Download Link   	                                                                             |                                           
|-------------|:--------------------------------------------------------------------------------------------:|
| 0.0.1      |[reportportal-client-0.0.1.jar](https://repo1.maven.org/maven2/com/theartos/reportportal-client/0.0.1/reportportal-client-0.0.1.jar) | 


# Recommended Artos version is 0.0.13 or above
| Release     |Download Link   	                                                                             |                                           
|-------------|:--------------------------------------------------------------------------------------------:|
| 0.0.13      |[artos-0.0.13.jar](https://repo1.maven.org/maven2/com/theartos/artos/0.0.13/artos-0.0.13.jar) | 

# How to setup ReportPortal-Client for ARTOS project

Step 1 : Ensure you are using Artos version 0.0.13 or above. 

Step 2 : Add report portal client to build path

* If you have simple java project then go to https://mvnrepository.com/artifact/com.theartos/reportportal-client and download the latest Jar. Add jar to project build path. 

* If you have maven project then copy Maven dependency from the https://mvnrepository.com/artifact/com.theartos/reportportal-client and add to your POM file

```xml
<dependency>
            <groupId>com.theartos</groupId>
            <artifactId>reportportal-client</artifactId>
            <version>0.0.1</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/lib/reportportal-client-0.0.1-SNAPSHOT.jar</systemPath>
</dependency>
```
Step 3 : Update Maven Project to ensure added Jars are downloaded

Step 4 : Create "reportportal_configuration.xml" file to project root directory ```./conf/```. 

Step 5 : Add following info into "reportportal_configuration.xml" file and update the information to match your Report Portal configuration. 

```xml

<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<configuration>
  <reportportal_info>
    <property name="Project_Name">PROJECT_NAME_IN_REPORT_PORTAL</property>
    <property name="TestSuite_Name">ARTOS_TESTSUITE_1</property>
    <property name="Release_Name">01.02.0002</property>
    <property name="Base_URL">http://192.168.1.23:8080</property>
    <property name="UUID">0abb483c-37f2-4abd-a029-3c7facfa7b7c</property>
  </reportportal_info>
</configuration>

```

Step 6 : Create Listener for Artos as shown below and add to your project

```java
/*******************************************************************************
 * Copyright (C) 2018-2019 Arpit Shah and Artos Contributors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package listener;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.artos.framework.Enums.TestStatus;
import com.artos.framework.infra.BDDScenario;
import com.artos.framework.infra.BDDStep;
import com.artos.framework.infra.TestObjectWrapper;
import com.artos.framework.infra.TestUnitObjectWrapper;
import com.artos.interfaces.TestProgress;
import com.google.common.base.Throwables;
import com.theartos.ReportPortalLauncher;
import com.theartos.ReportPortalLauncher.LogStatus;
import com.theartos.ReportPortalLauncher.Status;

public class ReportPortalListener implements TestProgress {

	ReportPortalLauncher rpl;
	boolean activeTest = false;
	boolean activeChildUnit = false;

	public ReportPortalListener() {

	}

	@Override
	public void testSuiteExecutionStarted(String description) {
		rpl = new ReportPortalLauncher();
		rpl.StartLaunch();

		String TestSuiteName = "TestSuite"; // Default value

		// Find Test Suite Name
		if (null != description) {
			String[] desc = description.split("\\.");
			int descLength = desc.length;
			if (descLength > 2) {
				TestSuiteName = desc[descLength - 2];
			} else {
				TestSuiteName = description;
			}
		}

		rpl.StartSuite(TestSuiteName, description);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void testSuiteExecutionFinished(String description) {
		rpl.endSuite();
		rpl.endLaunch();

		// Give some time for API to finish its communication
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void testCaseExecutionStarted(TestObjectWrapper t) {
		Set<String> tags = new HashSet<String>();
		for (String s : t.getGroupList()) {
			tags.add(s);
		}
		// Start next test
		rpl.StartTest(t.getTestClassObject().getName(),
				"".equals(t.getTestPlanDescription().trim()) ? t.getTestPlanBDD() : t.getTestPlanDescription(), tags);
		activeTest = true;
	}

	@Override
	public void testResult(TestObjectWrapper t, TestStatus testStatus, File snapshot, String description) {
		if (testStatus == TestStatus.PASS || testStatus == TestStatus.KTF) {
			rpl.endTest(Status.PASSED);
		} else if (testStatus == TestStatus.SKIP) {
			rpl.endTest(Status.SKIPPED);
		} else if (testStatus == TestStatus.FAIL) {
			rpl.endTest(Status.FAILED);
		}
	}

	@Override
	public void childTestUnitExecutionStarted(TestObjectWrapper t, TestUnitObjectWrapper unit, String paramInfo) {
		Set<String> tags = new HashSet<String>();
		for (String s : unit.getGroupList()) {
			tags.add(s);
		}
		rpl.StartStep(unit.getTestUnitMethod().getName() + " " + paramInfo,
				"".equals(unit.getTestPlanDescription()) ? unit.getTestPlanBDD() : unit.getTestPlanDescription(), tags);
		activeChildUnit = true;
	}

	@Override
	public void testUnitExecutionStarted(TestUnitObjectWrapper unit) {
		if (!activeChildUnit) {
			Set<String> tags = new HashSet<String>();
			for (String s : unit.getGroupList()) {
				tags.add(s);
			}
			rpl.StartStep(unit.getTestUnitMethod().getName(),
					"".equals(unit.getTestPlanDescription().trim()) ? unit.getTestPlanBDD()
							: unit.getTestPlanDescription(),
					tags);
		}
	}

	@Override
	public void testUnitResult(TestUnitObjectWrapper unit, TestStatus testStatus, File snapshot, String description) {
		if (testStatus == TestStatus.PASS || testStatus == TestStatus.KTF) {
			rpl.endStep(Status.PASSED);
		} else if (testStatus == TestStatus.SKIP) {
			rpl.endStep(Status.SKIPPED);
		} else if (testStatus == TestStatus.FAIL) {
			rpl.endStep(Status.FAILED);
		}
		activeChildUnit = false;
	}

	@Override
	public void testCaseStatusUpdate(TestStatus testStatus, File snapshot, String msg) {
		if (snapshot == null) {
			rpl.log(LogStatus.Info, msg);
		} else {
			rpl.log(LogStatus.Info, msg, snapshot);
		}
	}

	@Override
	public void testException(Throwable e) {
		rpl.log(LogStatus.Error, Throwables.getStackTraceAsString(e));
	}

	@Override
	public void unitException(Throwable e) {
		rpl.log(LogStatus.Error, Throwables.getStackTraceAsString(e));
	}

	@Override
	public void childTestUnitExecutionFinished(TestUnitObjectWrapper unit) {

	}

	@Override
	public void testUnitExecutionFinished(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childTestCaseExecutionStarted(TestObjectWrapper t, String paramInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childTestCaseExecutionFinished(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testCaseSummaryPrinting(String FQCN, String description) {

	}

	@Override
	public void testUnitSummaryPrinting(String FQCN, String description) {

	}

	@Override
	public void testSuiteSummaryPrinting(String description) {

	}

	@Override
	public void testExecutionLoopCount(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTestSuiteMethodExecutionStarted(String methodName, String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTestSuiteMethodExecutionFinished(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTestSuiteMethodExecutionStarted(String methodName, String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTestSuiteMethodExecutionFinished(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printTestPlan(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printTestPlan(BDDScenario sc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printTestUnitPlan(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printTestUnitPlan(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestUnitMethodExecutionStarted(String methodName, TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestUnitMethodExecutionStarted(String methodName, BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestUnitMethodExecutionFinished(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestUnitMethodExecutionFinished(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestUnitMethodExecutionStarted(String methodName, TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestUnitMethodExecutionStarted(String methodName, BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterFailedUnitMethodExecutionStarted(String methodName, TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterFailedUnitMethodExecutionStarted(String methodName, BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestUnitMethodExecutionFinished(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestUnitMethodExecutionFinished(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterFailedUnitMethodExecutionFinished(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterFailedUnitMethodExecutionFinished(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localBeforeTestUnitMethodExecutionStarted(TestObjectWrapper t, TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localBeforeTestUnitMethodExecutionFinished(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localAfterTestUnitMethodExecutionStarted(TestObjectWrapper t, TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localAfterTestUnitMethodExecutionFinished(TestUnitObjectWrapper unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestCaseMethodExecutionStarted(String methodName, TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestCaseMethodExecutionStarted(String methodName, BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestCaseMethodExecutionFinished(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalBeforeTestCaseMethodExecutionFinished(BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestCaseMethodExecutionStarted(String methodName, TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestCaseMethodExecutionStarted(String methodName, BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestCaseMethodExecutionFinished(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void globalAfterTestCaseMethodExecutionFinished(BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localBeforeTestCaseMethodExecutionStarted(String methodName, TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localBeforeTestCaseMethodExecutionFinished(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localAfterTestCaseMethodExecutionStarted(String methodName, TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localAfterTestCaseMethodExecutionFinished(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testCaseExecutionStarted(BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testCaseExecutionFinished(BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testUnitExecutionStarted(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testUnitExecutionFinished(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testCaseExecutionSkipped(TestObjectWrapper t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childTestCaseExecutionStarted(BDDScenario scenario, String paramInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childTestCaseExecutionFinished(BDDScenario scenario) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childTestUnitExecutionStarted(BDDScenario scenario, BDDStep step, String paramInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childTestUnitExecutionFinished(BDDStep step) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testResult(BDDScenario scenario, TestStatus testStatus, File snapshot, String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testCaseExecutionFinished(TestObjectWrapper t) {

	}

	@Override
	public void testSuiteFailureHighlight(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testSuiteException(Throwable e) {
		// TODO Auto-generated method stub

	}

}

```

Step 7 : Register listener via FeatureRunner Class

```java

	public static void main(String[] args) throws Exception {
		List<Class<?>> listners = Lists.newArrayList(ReportPortalListener.class);
		Runner runner = new Runner(FeatureRunner.class, listners);
		runner.setTestList(getTestList());
		runner.run(args);
	}
	
```

Step 8 : Execute the test suite and monitor the update in Report Portal

## Below is the Example code to test reportportal-client jar

```java
public class Test {

	public static void main(String[] args) {
		ReportPortalLauncher rpl = new ReportPortalLauncher();

		// Launch is mandatory
		rpl.StartLaunch();

		// Test Suite can have only one instance per run
		rpl.StartSuite("TestSuite", "Start of the Test Suite");

		// Execute BeforeTestSuite once test suite is constructed
		rpl.StartBeforeSuite("BeforeTestSuite", "Unit Before Test Suite");
		rpl.endBeforeSuite(Status.PASSED);

		Set<String> tags = new HashSet<String>();
		tags.add("Group1");
		tags.add("Group2");
		// Construct Test Case
		rpl.StartTest("TestStart", "Start of the Test Case", tags);

		// Execute Before Test once Test case object is constructed
		rpl.StartBeforeTest("BeforeTest", "Unit Before Test Test Execution");
		rpl.endBeforeTest(Status.PASSED);

		rpl.StartStep("TestStep 1", "Start of the Test Step 1", tags);
		// Execute BeforeMethod once Test Step object is constructed
		rpl.StartBeforeMethod("BeforeMethod", "Unit Before Test Test Execution");
		// log something
		rpl.log(LogStatus.Debug, "This is log line 1");
		rpl.log(LogStatus.Debug, "This is log line 2");
		rpl.endBeforeMethod(Status.PASSED);
		// Execute AfterMethod before Test Step object is ended
		rpl.StartAfterMethod("AfterMethod", "Unit Before Test Test Execution");
		rpl.endAfterMethod(Status.PASSED);
		rpl.endStep(Status.PASSED);

		rpl.StartStep("TestStep 2", "Start of the Test Step 2", tags);
		rpl.endStep(Status.FAILED);

		rpl.StartStep("TestStep 3", "Start of the Test Step 3", tags);
		rpl.endStep(Status.SKIPPED);

		rpl.StartStep("TestStep 4", "Start of the Test Step 4", tags);
		rpl.endStep(Status.PASSED);

		// Execute After Test before ending the Test case object
		rpl.StartAfterTest("AfterTest", "Unit After Test Test Execution");
		rpl.endAfterTest(Status.PASSED);

		// End Test Case
		rpl.endTest(Status.FAILED);

		// Execute BeforeTestSuite before test suite is ended
		rpl.StartAfterSuite("AfterTestSuite", "Unit After Test Suite");
		rpl.endAfterSuite(Status.PASSED);

		rpl.endSuite();

		rpl.endLaunch();

	}
}
```
