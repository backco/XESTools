/**
 *
 */

package org.qmpm.tools.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.qmpm.tools.XESTools;

// TODO: XESToolsTest

class XESToolsTest {

	static final String	LOGSORTPATH	= System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "testlog_sort.xes";

	static XLog			logSort;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {

		logSort = XESTools.loadXES(LOGSORTPATH);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void sortTest() throws IOException {

		XESTools.sortByTimeStampAndEventClass(logSort, Arrays.asList("A", "B", "C"));

		for (XTrace t : logSort) {

			for (XEvent e : t) {
				System.out.println("[" + XESTools.xEventTimeStamp(e) + "] " + XESTools.xEventName(e));
			}

			System.out.println("");
		}
	}

	@Test
	void serializeTest() throws IOException {

		XESTools.write(logSort, new FileOutputStream("test.xes"));
	}

}
