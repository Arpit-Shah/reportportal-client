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
package com.theartos;

import java.io.File;
import java.util.Calendar;
import java.util.Set;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

import io.reactivex.Maybe;

/**
 * This class implements API required to talk to ReportPortal
 * https://github.com/reportportal/documentation/blob/master/src/md/src/DevGuides/reporting.md
 * 
 * @author ArpitS
 *
 */
public class ReportPortalLauncher {

	public String projectName = "";
	public String releaseName = "";
	private String uUID;
	private String baseURL;
	private String testSuiteName;
	
	private ReportPortal reportPortalObj = null;
	private Maybe<String> launchUUID;
	private Maybe<String> StartBeforeSuiteUUID;
	private Maybe<String> StartAfterSuiteUUID;
	private Maybe<String> StartBeforeTestUUID;
	private Maybe<String> StartAfterTestUUID;
	private Maybe<String> StartBeforeMethodUUID;
	private Maybe<String> StartAfterMethodUUID;
	private Maybe<String> SuiteUUID;
	private Maybe<String> testUUID;
	private Maybe<String> stepUUID;
	private Maybe<String> childStepUUID;
	private Maybe<String> itemid;
	Launch launch;

	private static Status oStatus;
	private static LogStatus oLogStatus;

	public static enum Status {
		FAILED, STOPPED, SKIPPED, RESTED, CANCELLED, PASSED
	}

	public static enum LogStatus {
		Info, Error, Debug, Skipped, Warn, Fatal
	}

	/**
	 * Convert Enum to String
	 * 
	 * @param status = Log Status
	 * @return = String value of the status
	 */
	private String ConvertStatus(LogStatus logStatus) {
		switch (logStatus) {
		case Info:
			return "INFO";
		case Error:
			return "ERROR";
		case Warn:
			return "WARN";
		case Fatal:
			return "FATAL";
		case Debug:
			return "DEBUG";
		default:
			return "INFO";
		}
	}

	/**
	 * Convert Enum to String
	 * 
	 * @param status = Item Status
	 * @return = String value of the status
	 */
	private String ConvertSStatus(Status status) {
		switch (status) {
		case PASSED:
			return "PASSED";
		case FAILED:
			return "FAILED";
		case SKIPPED:
			return "SKIPPED";

		default:
			return "PASSED";
		}
	}

	public ReportPortalLauncher() {
		ReportPortalConfigParser parser = new ReportPortalConfigParser(true);
		this.projectName = parser.getProject_Name();
		this.testSuiteName = parser.getTestSuite_Name();
		this.releaseName = parser.getRelease_Name();
		this.uUID = parser.getUUID();
		this.baseURL = parser.getBase_URL();
	}

	// ********************************************
	// START LAUNCH
	// ********************************************
	/**
	 * Starts the client connection between ReportPortal and test system
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *    "name": "rp_launch",
	 *    "description": "My first launch on RP",
	 *    "startTime": "1574423221000",
	 *    "mode": "DEFAULT",
	 *    "attributes": [
	 *      {
	 *        "key": "build",
	 *        "value": "0.1"
	 *      },
	 *      {
	 *        "value": "test"
	 *      }   
	 *   ] 
	 * }
	 *
	 * RESPONSE
	 * {
	 *    "id": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *    "number": 1
	 * }
	 * </PRE>
	 * 
	 * @return = UUID of created launch
	 *         (Example:694e1549-b8ab-4f20-b7d8-8550c92431b0)
	 */
	public Maybe<String> StartLaunch() {
		// Create Parameter object with all required info
		ListenerParameters params = new ListenerParameters();
		{
			params.setBaseUrl(baseURL.trim());
			params.setUuid(uUID.trim());
			params.setLaunchName(testSuiteName);
			params.setProjectName(projectName);
			params.setDescription(testSuiteName + " FOR " + projectName);
			params.setLaunchRunningMode(Mode.DEFAULT);
			// Do not allow re-run
			params.setRerun(false);
			params.setEnable(true);
		}

		// Start launch request with given parameters
		StartLaunchRQ rq = new StartLaunchRQ();
		{
			rq.setName(params.getLaunchName());
			rq.setStartTime(Calendar.getInstance().getTime());
			// Launch mode. Allowable values 'default' or 'debug'
			rq.setMode(params.getLaunchRunningMode());
			rq.setDescription(params.getDescription());
			try {
				reportPortalObj = ReportPortal.builder().withParameters(params).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		launch = reportPortalObj.newLaunch(rq);
		launchUUID = launch.start();

		return launchUUID;
	}

	// ********************************************
	// END LAUNCH
	// ********************************************
	/**
	 * End Launch
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 */
	public void endLaunch() {
		FinishExecutionRQ fq = new FinishExecutionRQ();
		fq.setEndTime(Calendar.getInstance().getTime());
		launch.finish(fq);
	}

	public void endLaunch(Status status) {

		FinishExecutionRQ fq = new FinishExecutionRQ();
		fq.setEndTime(Calendar.getInstance().getTime());
		fq.setStatus(ConvertSStatus(status));
		launch.finish(fq);
	}

	// ********************************************
	// START BEFORE TEST SUITE
	// ********************************************
	/**
	 * Start Before Test Suite
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "before_suite",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Suite Name
	 * @param description = Suite Description
	 * @return = UUID of created item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeSuite(String name, String description) {
		StartBeforeSuiteUUID = startItem("BEFORE_SUITE", name, description, false, null);
		return StartBeforeSuiteUUID;
	}

	// ********************************************
	// END BEFORE TEST SUITE
	// ********************************************
	/**
	 * End Before Test Suite
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endBeforeSuite(Status status) {
		endItem(StartBeforeSuiteUUID, status);
	}

	// ********************************************
	// START AFTER TEST SUITE
	// ********************************************
	/**
	 * Start After Test Suite
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "before_suite",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Suite Name
	 * @param description = Suite Description
	 * @return = UUID of created item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterSuite(String name, String description) {
		StartAfterSuiteUUID = startItem("AFTER_SUITE", name, description, false, null);
		return StartAfterSuiteUUID;
	}

	// ********************************************
	// END AFTER TEST SUITE
	// ********************************************
	/**
	 * End After Test Suite
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endAfterSuite(Status status) {
		endItem(StartAfterSuiteUUID, status);
	}

	// ********************************************
	// START BEFORE TEST
	// ********************************************
	/**
	 * Start Before Test
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "before_test",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Name
	 * @param description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeTest(String name, String description) {
		StartBeforeTestUUID = startItem("BEFORE_TEST", name, description, false, null);
		return StartBeforeTestUUID;
	}

	// ********************************************
	// END BEFORE TEST
	// ********************************************
	/**
	 * End Before Test
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endBeforeTest(Status status) {
		endItem(StartBeforeTestUUID, status);
	}

	// ********************************************
	// START AFTER TEST
	// ********************************************
	/**
	 * Start After Test
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "after_test",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Name
	 * @param description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterTest(String name, String description) {
		StartAfterTestUUID = startItem("AFTER_TEST", name, description, false, null);
		return StartAfterTestUUID;
	}

	// ********************************************
	// END AFTER TEST
	// ********************************************
	/**
	 * End After Test
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endAfterTest(Status status) {
		endItem(StartAfterTestUUID, status);
	}

	// ********************************************
	// START BEFORE METHOD
	// ********************************************
	/**
	 * Start Before Method
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "before_method",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Method Name
	 * @param description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeMethod(String name, String description) {
		StartBeforeMethodUUID = startItem("BEFORE_METHOD", name, description, false, null);
		return StartBeforeMethodUUID;
	}

	// ********************************************
	// END BEFORE METHOD
	// ********************************************
	/**
	 * End Before Method
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endBeforeMethod(Status status) {
		endItem(StartBeforeMethodUUID, status);
	}

	// ********************************************
	// START AFTER METHOD
	// ********************************************
	/**
	 * Start After Method
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "after_method",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Method Name
	 * @param description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterMethod(String name, String description) {
		StartAfterMethodUUID = startItem("AFTER_METHOD", name, description, false, null);
		return StartAfterMethodUUID;
	}

	// ********************************************
	// END AFTER METHOD
	// ********************************************
	/**
	 * End After Method
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endAfterMethod(Status status) {
		endItem(StartAfterMethodUUID, status);
	}

	// ********************************************
	// START SUITE
	// ********************************************
	/**
	 * Start Test Suite
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "suite",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Suite Name
	 * @param description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartSuite(String name, String description) {
		SuiteUUID = startItem("SUITE", name, description, false, null);
		return SuiteUUID;
	}

	// ********************************************
	// END SUITE
	// ********************************************
	/**
	 * End Suite
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 */
	public void endSuite() {
		endItem(SuiteUUID, null);
	}

	/**
	 * End Suite
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endSuite(Status status) {
		endItem(SuiteUUID, status);
	}

	// ********************************************
	// START TEST
	// ********************************************
	/**
	 * Start Test
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "test",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Suite Name
	 * @param description = Suite Description
	 * @param tags        = tags or group
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartTest(String name, String description, Set<String> tags) {
		testUUID = startItem("TEST", name, description, false, tags);
		return testUUID;
	}

	// ********************************************
	// END TEST
	// ********************************************
	/**
	 * End Test
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endTest(Status status) {
		endItem(testUUID, status);
	}

	// ********************************************
	// START STEP
	// ********************************************
	/**
	 * Start Step
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "step",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Suite Name
	 * @param description = Suite Description
	 * @param tags        = tags or groups
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartStep(String name, String description, Set<String> tags) {
		stepUUID = startItem("STEP", name, description, false, tags);
		return stepUUID;
	}

	// ********************************************
	// END STEP
	// ********************************************
	/**
	 * End Step
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endStep(Status status) {
		endItem(stepUUID, status);
	}

	public void endStep(String Description, Status status) {
		FinishTestItemRQ ftep = new FinishTestItemRQ();
		ftep.setEndTime(Calendar.getInstance().getTime());
		ftep.setStatus(ConvertSStatus(status));
		ftep.setDescription(Description);
		launch.finishTestItem(stepUUID, ftep);
	}

	// ********************************************
	// START CHILD STEP
	// ********************************************
	/**
	 * Start Child Step
	 * 
	 * <PRE>
	 * REQUEST
	 * {
	 *  "name": "Services",
	 *  "startTime": "1574423234000",
	 *  "type": "step",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4",
	 *  "description": "Services tests"
	 * }
	 * 
	 * RESPONSE
	 * {
	 *  "id": "1e183148-c79f-493a-a615-2c9a888cb441"
	 * }
	 * </PRE>
	 * 
	 * @param name        = Test Suite Name
	 * @param description = Suite Description
	 * @param tags        = tags or groups
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartChildStep(String name, String description, Set<String> tags) {
		childStepUUID = startItem("STEP", name, description, true, tags);
		return childStepUUID;
	}

	// ********************************************
	// END CHILD STEP
	// ********************************************
	/**
	 * End Child Step
	 * 
	 * <PRE>
	 * {
	 *  "endTime": "1574423247000",
	 *  "launchUuid": "96d1bc02-6a3f-451e-b706-719149d51ce4"
	 * }
	 * </PRE>
	 * 
	 * @param status = Result Status
	 */
	public void endChildStep(Status status) {
		endItem(childStepUUID, status);
	}

	public void endChildStep(String description, Status status) {
		FinishTestItemRQ ftep = new FinishTestItemRQ();
		ftep.setEndTime(Calendar.getInstance().getTime());
		ftep.setStatus(ConvertSStatus(status));
		ftep.setDescription(description);
		launch.finishTestItem(childStepUUID, ftep);
	}

	// ********************************************
	// Log
	// ********************************************

	/**
	 * Log lines can only be added for active component. For Example: If Test has
	 * steps inside then logs can only be added to the steps, If Test does not have
	 * steps then logs can only be added to tests
	 * 
	 * @param logStatus = LogStatus
	 * @param message   = Log Message
	 */
	public void log(LogStatus logStatus, String message) {
		ReportPortal.emitLog(message, ConvertStatus(logStatus), Calendar.getInstance().getTime());
	}

	/**
	 * Log lines can only be added for active component. For Example: If Test has
	 * steps inside then logs can only be added to the steps, If Test does not have
	 * steps then logs can only be added to tests
	 * 
	 * @param logStatus = LogStatus
	 * @param message   = Log Message
	 * @param f         = log file
	 */
	public void log(LogStatus logStatus, String message, File f) {
		ReportPortal.emitLog(message, ConvertStatus(logStatus), Calendar.getInstance().getTime(), f);
	}

	// ********************************************
	// Restart Launch
	// ********************************************

	public Maybe<String> ReStartLaunch(Maybe<String> elid) {
		launch = reportPortalObj.withLaunch(elid);
		launchUUID = launch.start();
		return launchUUID;
	}

	public Maybe<String> ReStartSuite(Maybe<String> esid, String Name, String Description) {
		StartTestItemRQ st = new StartTestItemRQ();
		st.setName(Name);
		st.setDescription(Description);
		st.setStartTime(Calendar.getInstance().getTime());
		// st.setRetry(true);
		st.setType("SUITE");
		SuiteUUID = launch.startTestItem(esid, st);
		return SuiteUUID;
	}

	/**
	 * Creates Item on demand with fix hierarchy as shown below
	 * 
	 * <PRE>
	 * Suite = BeforeSuite + AfterSuite + Test(s)
	 * Test = BeforeTest + AfterTest + Step(s)
	 * Step = BeforeMethod + AfterMethod
	 * </PRE>
	 * 
	 * @param item        = One of the following option ("SUITE, STORY, TEST,
	 *                    SCENARIO, STEP, BEFORE_CLASS, BEFORE_GROUPS," +
	 *                    "BEFORE_METHOD, BEFORE_SUITE, BEFORE_TEST, AFTER_CLASS,
	 *                    AFTER_GROUPS, AFTER_METHOD, AFTER_SUITE, AFTER_TEST")
	 * @param name        = Name of the item
	 * @param description = Description of the item
	 * @param child       = If child step then true otherwise false
	 * @param tags        = String tags
	 * @return = Item UUID
	 */
	public Maybe<String> startItem(String item, String name, String description, boolean child, Set<String> tags) {

		StartTestItemRQ st = new StartTestItemRQ();
		st.setDescription(description);
		st.setName(name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType(item);
		if (null != tags) {
			st.setTags(tags);
		}

		// Suite => BeforeSuite + AfterSuite + Test(s)
		// Test => BeforeTest + AfterTest + Step(s)
		// Step => BeforeMethod + AfterMethod
		if ("SUITE".equals(item)) {
			itemid = launch.startTestItem(st);
		} else if ("BEFORE_SUITE".equals(item)) {
			itemid = launch.startTestItem(SuiteUUID, st);
		} else if ("AFTER_SUITE".equals(item)) {
			itemid = launch.startTestItem(SuiteUUID, st);
		} else if ("TEST".equals(item)) {
			itemid = launch.startTestItem(SuiteUUID, st);
		} else if ("STEP".equals(item)) {
			if (child) {
				itemid = launch.startTestItem(stepUUID, st);
			} else {
				itemid = launch.startTestItem(testUUID, st);
			}
		} else if ("BEFORE_TEST".equals(item)) {
			itemid = launch.startTestItem(testUUID, st);
		} else if ("AFTER_TEST".equals(item)) {
			itemid = launch.startTestItem(testUUID, st);
		} else if ("BEFORE_METHOD".equals(item)) {
			itemid = launch.startTestItem(stepUUID, st);
		} else if ("AFTER_METHOD".equals(item)) {
			itemid = launch.startTestItem(stepUUID, st);
		}
		return itemid;
	}

	/**
	 * Ends specified item
	 * 
	 * @param itemUUID = Item UUID that needs to be ended
	 * @param status   = Status update if required
	 */
	public void endItem(Maybe<String> itemUUID, Status status) {
		FinishTestItemRQ ftep = new FinishTestItemRQ();
		ftep.setEndTime(Calendar.getInstance().getTime());
		if (status != null) {
			ftep.setStatus(ConvertSStatus(status));
		}
		launch.finishTestItem(itemUUID, ftep);
	}

	// =====================================================
	// Getters and Setters
	// =====================================================

	public Status getStatus() {
		return oStatus;
	}

	public LogStatus getLogStatus() {
		return oLogStatus;
	}

	public Maybe<String> getStartBeforeSuiteUUID() {
		StartBeforeSuiteUUID.subscribe(s -> System.out.print("StartBeforeSuiteUUID : " + s));
		return StartBeforeSuiteUUID;
	}

	public Maybe<String> getStartAfterSuiteUUID() {
		StartAfterSuiteUUID.subscribe(s -> System.out.print("StartAfterSuiteUUID : " + s));
		return StartAfterSuiteUUID;
	}

	public Maybe<String> getStartBeforeTestUUID() {
		StartBeforeTestUUID.subscribe(s -> System.out.print("StartBeforeTestUUID : " + s));
		return StartBeforeTestUUID;
	}

	public Maybe<String> getStartAfterTestUUID() {
		StartAfterTestUUID.subscribe(s -> System.out.print("StartAfterTestUUID : " + s));
		return StartAfterTestUUID;
	}

	public Maybe<String> getStartBeforeMethodUUID() {
		StartBeforeMethodUUID.subscribe(s -> System.out.print("StartBeforeMethodUUID : " + s));
		return StartBeforeMethodUUID;
	}

	public Maybe<String> getStartAfterMethodUUID() {
		StartAfterMethodUUID.subscribe(s -> System.out.print("StartAfterMethodUUID : " + s));
		return StartAfterMethodUUID;
	}

	public String getuUID() {
		System.out.print(uUID);
		return uUID;
	}

	public Maybe<String> getLaunchUUID() {
		launchUUID.subscribe(s -> System.out.print("LaunchUUID : " + s));
		return launchUUID;
	}

	public Maybe<String> getSuiteUUID() {
		SuiteUUID.subscribe(s -> System.out.print("SuiteUUID : " + s));
		return SuiteUUID;
	}

	public Maybe<String> getTestUUID() {
		testUUID.subscribe(s -> System.out.print("TestUUID : " + s));
		return testUUID;
	}

	public Maybe<String> getStepUUID() {
		stepUUID.subscribe(s -> System.out.print("StepUUID : " + s));
		return stepUUID;
	}

	public void printUUID(Maybe<String> uuid, String description) {
		uuid.subscribe(s -> System.out.print(description + " : " + s));
	}

	public Maybe<String> getLaunchId() {
		launchUUID.subscribe(s -> System.out.print("launchUUID : " + s));
//		launchUUID.subscribe(x -> {
//			// If successful then assign value to testParent
//			testParent = x;
//			System.out.print("Emitted item: " + x);
//		}, ex -> {
//			// If error then assign null value to testParent
//			testParent = null;
//			System.out.println("Error: " + ex.getMessage());
//		}, () -> {
//			// If empty then assign null value to testParent
//			testParent = null;
//			System.out.println("Completed. No items.");
//		});
		return launchUUID;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getTestSuiteName() {
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public void setuUID(String uUID) {
		this.uUID = uUID;
	}
}
