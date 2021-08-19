/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package nl.haarlem.translations.zdstozgw;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.nn.testtool.Checkpoint;
import nl.nn.testtool.Report;
import nl.nn.testtool.TestTool;
import nl.nn.testtool.run.ReportRunner;
import nl.nn.testtool.run.RunResult;
import nl.nn.testtool.storage.CrudStorage;
import nl.nn.testtool.storage.Storage;
import nl.nn.testtool.storage.StorageException;
import nl.nn.testtool.transform.ReportXmlTransformer;

/**
 * Call Ladybug to run the reports present in the test storage (see Test tab in Ladybug) as JUnit test
 * 
 * @author Jaco de Groot
 */
@SpringBootTest
public class LadybugTests {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Autowired
	private TestTool testTool;
	@Autowired
	private CrudStorage testStorage;
	@Autowired
	private Storage debugStorage; 
	@Autowired
	private ReportXmlTransformer reportXmlTransformer;

	@Test
	public void runAllTestReports() {
		runTestReports(null);
	}

	@Test
	public void runGenereerZaakIdentificatieTestReport() {
		runTestReports((Report report) -> {return !report.getName().contains("genereerZaakIdentificatie");});
	}

	@Test
	public void runCreeerZaakTestReport() {
		runTestReports((Report report) -> {return !report.getName().contains("creeerZaak");});
	}

	@Test
	public void runVoegZaakdocumentToeTestReport() {
		runTestReports((Report report) -> {return !report.getName().contains("voegZaakdocumentToe");});
	}

	@Test
	public void runGeefZaakdetailsTestReport() {
		runTestReports((Report report) -> {return !report.getName().contains("geefZaakdetails");});
	}

	private void runTestReports(Predicate<? super Report> filter) {
		List<Report> reports = new ArrayList<Report>();
		try {
			List<Integer> storageIds = testStorage.getStorageIds();
			for (Integer storageId : storageIds) {
				Report report = testStorage.getReport(storageId);
				reports.add(report);
			}
		} catch (StorageException e) {
			fail(e.getMessage());
		}
		assertTrue("No reports found", reports.size() > 0);
		Collections.sort(reports, new ReportNameComparator());
		if (filter != null) {
			reports.removeIf(filter);
			assertEquals(1, reports.size());
		}
		long totalTime = 0;
		ReportRunner reportRunner = new ReportRunner();
		reportRunner.setTestTool(testTool);
		reportRunner.setDebugStorage(debugStorage);
		reportRunner.run(reports, false, true);
		for (Report report : reports) {
			RunResult runResult = reportRunner.getResults().get(report.getStorageId());
			if (runResult.errorMessage != null) {
				fail(runResult.errorMessage);
			} else {
				Report runResultReport = null;
				try {
					runResultReport = reportRunner.getRunResultReport(runResult.correlationId);
				} catch (StorageException e) {
					fail(e.getMessage());
				}
				if (runResultReport == null) {
					fail("Result report not found. Report generator not enabled?");
				} else {
					report.setGlobalReportXmlTransformer(reportXmlTransformer);
					runResultReport.setGlobalReportXmlTransformer(reportXmlTransformer);
					runResultReport.setTransformation(report.getTransformation());
					runResultReport.setReportXmlTransformer(report.getReportXmlTransformer());
					long runResultTime = runResultReport.getEndTime() - runResultReport.getStartTime();
					totalTime += runResultTime;
					if (log.isInfoEnabled()) {
						int stubbed = 0;
						boolean first = true;
						for (Checkpoint checkpoint : runResultReport.getCheckpoints()) {
							if (first) {
								first = false;
							} else if (checkpoint.isStubbed()) {
								stubbed++;
							}
						}
						int total = runResultReport.getCheckpoints().size() - 1;
						String stubInfo = " (" + stubbed + "/" + total + " stubbed)";
						log.info("Assert " + report.getName() + " (" + (report.getEndTime() - report.getStartTime())
								+ " >> " + runResultTime + " ms)" + stubInfo);
					}
					assertEquals(report.toXml(reportRunner), runResultReport.toXml(reportRunner));
				}
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Total time: " + totalTime);
		}
	}
}

class ReportNameComparator implements Comparator<Report> {

	@Override
	public int compare(Report o1, Report o2) {
		return o1.getFullPath().compareTo(o2.getFullPath());
	}
}