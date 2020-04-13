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
	private Maybe<String> itemid;
	String launchName;
	public Set<String> Tags;
	public Set<String> stepTags;
	public Set<String> testTags;
	public Set<String> suiteTags;
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
	 * Constructor
	 * 
	 * @param launchName  = Name of launch (Example: AutomationRun)
	 * @param projectName = Same name as launch name created in Report Portal IO
	 *                    website
	 * @param releaseName = Release Name
	 * @param uUID        = Launch uuid (string identificator) (Example :
	 *                    69dc75cd-4522-44b9-9015-7685ec0e1abb)
	 * @param sURL        = Base URL
	 */
	public ReportPortalLauncher(final String launchName, final String projectName, final String releaseName,
			final String uUID, final String sURL) {
		this.projectName = projectName;
		this.releaseName = releaseName;
		this.uUID = uUID;
		this.baseURL = sURL;
		this.launchName = launchName;
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
			params.setLaunchName(launchName);
			params.setProjectName(projectName);
			params.setDescription(launchName + " FOR " + projectName);
			params.setLaunchRunningMode(Mode.DEFAULT);
			// Do not allow re-run
			params.setRerun(false);
			params.setEnable(true);
			if (Tags != null) {
				params.setTags(Tags);
			}
		}

		// Start launch request with given parameters
		StartLaunchRQ rq = new StartLaunchRQ();
		{
			rq.setName(params.getLaunchName());
			rq.setStartTime(Calendar.getInstance().getTime());
			// Launch mode. Allowable values 'default' or 'debug'
			rq.setMode(params.getLaunchRunningMode());
			if (Tags != null) {
				rq.setTags(params.getTags());
			}
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
	 * @param Name        = Test Suite Name
	 * @param Description = Suite Description
	 * @return = UUID of created item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeSuite(String Name, String Description) {
		StartBeforeSuiteUUID = startItem("BEFORE_SUITE", Name, Description);
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
		printUUID(StartBeforeSuiteUUID, "Ending StartBeforeSuiteUUID");
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
	 * @param Name        = Test Suite Name
	 * @param Description = Suite Description
	 * @return = UUID of created item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterSuite(String Name, String Description) {
		StartAfterSuiteUUID = startItem("AFTER_SUITE", Name, Description);
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
		printUUID(StartAfterSuiteUUID, "Ending StartAfterSuiteUUID");
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
	 * @param Name        = Test Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeTest(String Name, String Description) {
		StartBeforeTestUUID = startItem("BEFORE_TEST", Name, Description);
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
		printUUID(StartBeforeTestUUID, "Ending StartBeforeTestUUID");
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
	 * @param Name        = Test Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterTest(String Name, String Description) {
		StartAfterTestUUID = startItem("AFTER_TEST", Name, Description);
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
		printUUID(StartAfterTestUUID, "Ending StartAfterTestUUID");
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
	 * @param Name        = Method Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeMethod(String Name, String Description) {
		StartBeforeMethodUUID = startItem("BEFORE_METHOD", Name, Description);
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
		printUUID(StartBeforeMethodUUID, "Ending StartBeforeMethodUUID");
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
	 * @param Name        = Method Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterMethod(String Name, String Description) {
		StartAfterMethodUUID = startItem("AFTER_METHOD", Name, Description);
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
		printUUID(StartAfterMethodUUID, "Ending StartAfterMethodUUID");
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
	 * @param Name        = Test Suite Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartSuite(String Name, String Description) {
		SuiteUUID = startItem("SUITE", Name, Description);
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
		printUUID(SuiteUUID, "Ending SuiteUUID");
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
		printUUID(SuiteUUID, "Ending SuiteUUID");
		endItem(SuiteUUID, status);
	}

	// ********************************************
	// START TEST
	// ********************************************
	/**
	 * Start Test Suite
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
	 * @param Name        = Test Suite Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartTest(String Name, String Description) {
		testUUID = startItem("TEST", Name, Description);
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
		printUUID(testUUID, "Ending testUUID");
		endItem(testUUID, status);
	}

	// ********************************************
	// START STEP
	// ********************************************
	/**
	 * Start Test Suite
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
	 * @param Name        = Test Suite Name
	 * @param Description = Suite Description
	 * @return = UUID of created Item (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartStep(String Name, String Description) {
		stepUUID = startItem("STEP", Name, Description);
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
		printUUID(stepUUID, "Ending stepUUID");
		endItem(stepUUID, status);
	}

	public void endStep(String Description, Status status) {
		FinishTestItemRQ ftep = new FinishTestItemRQ();
		ftep.setEndTime(Calendar.getInstance().getTime());
		ftep.setStatus(ConvertSStatus(status));
		ftep.setDescription(Description);
		launch.finishTestItem(stepUUID, ftep);
	}

	public void log(String message, LogStatus status) {
		ReportPortal.emitLog(message, ConvertStatus(status), Calendar.getInstance().getTime());
	}

	public void log(String message, LogStatus status, File f) {
		ReportPortal.emitLog(message, ConvertStatus(status), Calendar.getInstance().getTime(), f);
	}

	private String ConvertStatus(LogStatus S) {
		switch (S) {
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
	 * @param Name        = Name of the item
	 * @param Description = Description of the item
	 * @return = Item UUID
	 */
	public Maybe<String> startItem(String item, String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();
		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType(item);

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
			itemid = launch.startTestItem(testUUID, st);
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

	public Status getStatus() {
		return oStatus;
	}

	public LogStatus getLogStatus() {
		return oLogStatus;
	}

	public Set<String> getTags() {
		return this.Tags;
	}

	public void setTags(Set<String> s) {
		this.Tags = s;
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

	public Maybe<String> getLaucnId() {
		launchUUID.subscribe(s -> System.out.print("launchUUID : " + s));
		return launchUUID;
	}
}
