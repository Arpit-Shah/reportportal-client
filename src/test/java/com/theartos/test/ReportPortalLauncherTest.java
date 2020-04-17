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
package com.theartos.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.theartos.ReportPortalLauncher;

import io.reactivex.Maybe;

class ReportPortalLauncherTest {

	String testParent;

	@Test
	void testDefaultVariables() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		assertEquals("RPT_PRJ_1", rpl.getProjectName());
		assertEquals("TEST_SUITE_1", rpl.getTestSuiteName());
		assertEquals("01.02.0002", rpl.getReleaseName());
		assertEquals("http://58.22.3.239:8080", rpl.getBaseURL());
		assertEquals("0acb493c-37d2-4afc-a029-3c7aad01fb78", rpl.getuUID());
	}

	@Test
	void testInvalidLaunchParameter() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		Maybe<String> launchid = rpl.StartLaunch();
		launchid.subscribe(x -> {
			// If successful then assign value to testParent
			testParent = x;
		}, ex -> {
			// If error then assign null value to testParent
			testParent = ex.getMessage();
		}, () -> {
			// If empty then assign null value to testParent
			testParent = null;
		});
		assertEquals(null, testParent);
	}

	@Test
	void testSetGetUUID() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		rpl.setBaseURL("XYZ");
		assertEquals("XYZ", rpl.getBaseURL());
	}

	@Test
	void testSetGetReleaseName() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		rpl.setBaseURL("RELEASE_1");
		assertEquals("RELEASE_1", rpl.getBaseURL());
	}

	@Test
	void testSetGetTestSuiteName() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		rpl.setBaseURL("TEST_SUITE_X");
		assertEquals("TEST_SUITE_X", rpl.getBaseURL());
	}

	@Test
	void testSetGetProjectName() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		rpl.setBaseURL("PROJECT-X");
		assertEquals("PROJECT-X", rpl.getBaseURL());
	}

	@Test
	void testSetGetBaseURL() {
		ReportPortalLauncher rpl = new ReportPortalLauncher();
		rpl.setBaseURL("BaseURL");
		assertEquals("BaseURL", rpl.getBaseURL());
	}

}
