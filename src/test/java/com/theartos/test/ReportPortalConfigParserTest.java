package com.theartos.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.theartos.ReportPortalConfigParser;

class ReportPortalConfigParserTest {

	@Test
	void testDefaultVariables() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		assertEquals("RPT_PRJ_1", rpcp.getProject_Name());
		assertEquals("TEST_SUITE_1", rpcp.getTestSuite_Name());
		assertEquals("01.02.0002", rpcp.getRelease_Name());
		assertEquals("http://58.22.3.239:8080", rpcp.getBase_URL());
		assertEquals("0acb493c-37d2-4afc-a029-3c7aad01fb78", rpcp.getUUID());
		assertEquals("." + File.separator + "conf" + File.separator, rpcp.getConfigBaseDir());
	}

	@Test
	void testSetGetUUID() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		rpcp.setUUID("XYZ");
		assertEquals("XYZ", rpcp.getUUID());
	}

	@Test
	void testSetGetReleaseName() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		rpcp.setRelease_Name("RELEASE_1");
		assertEquals("RELEASE_1", rpcp.getRelease_Name());
	}

	@Test
	void testSetGetTestSuiteName() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		rpcp.setTestSuite_Name("TEST_SUITE_X");
		assertEquals("TEST_SUITE_X", rpcp.getTestSuite_Name());
	}

	@Test
	void testSetGetProjectName() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		rpcp.setProject_Name("PROJECT-X");
		assertEquals("PROJECT-X", rpcp.getProject_Name());
	}

	@Test
	void testSetGetBaseURL() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		rpcp.setBase_URL("BaseURL");
		assertEquals("BaseURL", rpcp.getBase_URL());
	}

	@Test
	void testGetXMLFIle() {
		ReportPortalConfigParser rpcp = new ReportPortalConfigParser(true);
		File f = rpcp.getfXmlFile();
		File ref = new File(rpcp.getConfigBaseDir() + "reportportal_configuration.xml");
		assertEquals(ref.getAbsolutePath(), f.getAbsolutePath());
	}
}
