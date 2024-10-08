/*
  A BeepBeep palette to evaluate hypercompliance queries.
  Copyright (C) 2023 Sylvain Hallé and Chukri Soueidi

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package hypercompliancelab.school.process;


import hypercompliancelab.school.process.entities.Applicant;
import hypercompliancelab.school.process.logging.XesLogger;
import hypercompliancelab.school.process.pickers.Pickers;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.HardDisk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class for the school admission process. It creates an Activiti process engine and runs the process.
 */
public class SchoolAdmissionProcess {

	public static void main(String[] args) throws FileSystemException, IOException {

		int instances = 1000;
		HardDisk hd = new HardDisk("data").open();
		OutputStream os = hd.writeTo("log.xes");
		runAndProduceLogs(instances, os);
		os.close();
		hd.close();

	}

	public static void runAndProduceLogs(int instances, OutputStream os) {
		ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration().setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000").setJdbcUsername("sa").setJdbcPassword("").setJdbcDriver("org.h2.Driver").setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		ProcessEngine processEngine = cfg.buildProcessEngine();
		String pName = processEngine.getName();
		String ver = ProcessEngine.VERSION;
		System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		processEngine.getTaskService();

		repositoryService.createDeployment().addClasspathResource("school_admission.bpmn20.xml").deploy();

		for (int i = 0; i < instances; i++) {
			Map<String, Object> variables = new HashMap<>();

			Applicant applicant = Pickers.getApplicant(instances);
			variables.put("applicant", applicant);
			variables.put("isInternational", applicant.getIsInternational());
			if (applicant.getGPA() < 2) {
				variables.put("lowGPA", true);
			} else {
				variables.put("lowGPA", false);
			}

			variables.put("employee", "");

			runtimeService.startProcessInstanceByKey("school_Admission", variables);
		}

		try {
					XesLogger.getInstance().saveLog(os);
		} catch (IOException e) {
			System.out.println("Error saving file");
		}
	}
}
