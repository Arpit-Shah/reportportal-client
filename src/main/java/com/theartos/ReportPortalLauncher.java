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
import com.theartos.ReportPortalLauncher.Status;

import io.reactivex.Maybe;

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

	public Maybe<String> getLaucnId() {
		return launchUUID;

	}

	public static enum Status {
		Failed, STOPPED, SKIPPED, RESTED, CANCELLED, PASSED
	}

	public static enum LogStatus {
		Info, Error, Debug, Skipped, Warn, Fatal
	}

	/**
	 * 
	 * @param launchName  = Name of launch (Example: AutomationRun)
	 * @param projectName
	 * @param releaseName
	 * @param uUID        = Launch uuid (string identificator) (Example :
	 *                    69dc75cd-4522-44b9-9015-7685ec0e1abb)
	 * @param sURL
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
	 * Populate information which is needed to start a launch
	 * https://github.com/reportportal/documentation/blob/master/src/md/src/DevGuides/reporting.md
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

	public void endLaunch(Status Status) {

		FinishExecutionRQ fq = new FinishExecutionRQ();
		fq.setEndTime(Calendar.getInstance().getTime());
		fq.setStatus(ConvertSStatus(Status));
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeSuite(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("BEFORE_SUITE");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		StartBeforeSuiteUUID = launch.startTestItem(st);
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
	 */
	public void endBeforeSuite(Status Status) {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		ftc.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(StartBeforeSuiteUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterSuite(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("AFTER_SUITE");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		StartAfterSuiteUUID = launch.startTestItem(st);
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
	 */
	public void endAfterSuite(Status Status) {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		ftc.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(StartAfterSuiteUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeTest(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("BEFORE_TEST");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		StartBeforeTestUUID = launch.startTestItem(st);
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
	 */
	public void endBeforeTest(Status Status) {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		ftc.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(StartBeforeTestUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterTest(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("AFTER_TEST");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		StartAfterTestUUID = launch.startTestItem(st);
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
	 */
	public void endAfterTest(Status Status) {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		ftc.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(StartAfterTestUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartBeforeMethod(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("BEFORE_METHOD");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		StartBeforeMethodUUID = launch.startTestItem(st);
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
	 */
	public void endBeforeMethod(Status Status) {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		launch.finishTestItem(StartBeforeMethodUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartAfterMethod(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("AFTER_METHOD");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		StartAfterMethodUUID = launch.startTestItem(st);
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
	 */
	public void endAfterMethod() {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		launch.finishTestItem(StartAfterMethodUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartSuite(String Name, String Description) {

		StartTestItemRQ st = new StartTestItemRQ();

		st.setDescription(Description);
		st.setName(Name);
		st.setStartTime(Calendar.getInstance().getTime());
		st.setRetry(false);
		st.setType("SUITE");

		if (suiteTags != null) {
			st.setTags(suiteTags);
		}

		SuiteUUID = launch.startTestItem(st);

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
		FinishTestItemRQ ft = new FinishTestItemRQ();
		ft.setEndTime(Calendar.getInstance().getTime());
		launch.finishTestItem(SuiteUUID, ft);
	}

	public void endSuite(Status Status) {
		FinishTestItemRQ ft = new FinishTestItemRQ();
		ft.setEndTime(Calendar.getInstance().getTime());
		ft.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(SuiteUUID, ft);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartTest(String Name, String Desc) {

		StartTestItemRQ testcase = new StartTestItemRQ();

		testcase.setDescription(Desc);
		testcase.setName(Name);
		testcase.setStartTime(Calendar.getInstance().getTime());
		testcase.setRetry(false);
		testcase.setType("TEST");

		if (testTags != null) {
			testcase.setTags(testTags);
		}

		testUUID = launch.startTestItem(SuiteUUID, testcase);
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
	 */
	public void endTest(Status Status) {
		FinishTestItemRQ ftc = new FinishTestItemRQ();
		ftc.setEndTime(Calendar.getInstance().getTime());
		ftc.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(testUUID, ftc);
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
	 * @return = UUID of created Suite
	 *         (Example:1e183148-c79f-493a-a615-2c9a888cb441)
	 */
	public Maybe<String> StartStep(String Name, String Description) {

		StartTestItemRQ step = new StartTestItemRQ();

		step.setDescription(Description);
		step.setName(Name);
		step.setStartTime(Calendar.getInstance().getTime());
		step.setRetry(false);
		step.setType("STEP");

		if (stepTags != null) {
			step.setTags(stepTags);
		}

		stepUUID = launch.startTestItem(testUUID, step);
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
	 */
	public void endStep(Status Status) {
		FinishTestItemRQ ftep = new FinishTestItemRQ();
		ftep.setEndTime(Calendar.getInstance().getTime());
		ftep.setStatus(ConvertSStatus(Status));
		launch.finishTestItem(stepUUID, ftep);
	}
	
	public void endStep(String Description, Status Status) {
		FinishTestItemRQ ftep = new FinishTestItemRQ();
		ftep.setEndTime(Calendar.getInstance().getTime());
		ftep.setStatus(ConvertSStatus(Status));
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

	private String ConvertSStatus(Status S) {
		switch (S) {
		case PASSED:
			return "PASSED";
		case Failed:
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
//
//	public Maybe<String> startItem(String item, String Name, String Description) {
//		StartTestItemRQ step = new StartTestItemRQ();
//		step.setDescription(Description);
//		step.setName(Name);
//		step.setStartTime(Calendar.getInstance().getTime());
//		step.setRetry(false);
//		step.setType(item);
//		itemid = launch.startTestItem(testUUID, step); // under test
//		return itemid;
//	}
//
//	public Maybe<String> startItem(boolean test, String item, String Name, String Description) {
//		StartTestItemRQ step = new StartTestItemRQ();
//		step.setDescription(Description);
//		step.setName(Name);
//		step.setStartTime(Calendar.getInstance().getTime());
//		step.setRetry(false);
//		step.setType(item);
//
//		if (test)
//			itemid = launch.startTestItem(launchUUID, step); // suite level under launch
//		else
//			itemid = launch.startTestItem(SuiteUUID, step); // under suite
//		return itemid;
//	}
//
//	public void endItem(Status Status) {
//		FinishTestItemRQ ftep = new FinishTestItemRQ();
//		ftep.setEndTime(Calendar.getInstance().getTime());
//		if (Status != null)
//			ftep.setStatus(ConvertSStatus(Status));
//
//		launch.finishTestItem(itemid, ftep);
//
//	}
//
//	public void endItem(String Description, Status Status) {
//		FinishTestItemRQ ftep = new FinishTestItemRQ();
//		ftep.setEndTime(Calendar.getInstance().getTime());
//		ftep.setDescription(Description);
//
//		if (Status != null)
//			ftep.setStatus(ConvertSStatus(Status));
//
//		launch.finishTestItem(itemid, ftep);
//
//	}
	
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
}
