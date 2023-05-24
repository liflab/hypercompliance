package hypercompliancelab.school.process;


import hypercompliancelab.school.process.entities.Applicant;
import hypercompliancelab.school.process.logging.XesLogger;
import hypercompliancelab.school.process.pickers.Pickers;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.helpers.NOPLogger;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class for the school admission process. It creates an Activiti process engine and runs the process.
 */
public class SchoolAdmissionProcess {

    public static void main(String[] args) {

        int instances = 1000;
        runAndProduceLogs(instances, "data/log.xes");

    }

    public static void runAndProduceLogs(int instances, String filePathName) {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration().setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000").setJdbcUsername("sa").setJdbcPassword("").setJdbcDriver("org.h2.Driver").setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");

        RepositoryService repositoryService = processEngine.getRepositoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();

        // Deploy the process definition
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("school_admission.bpmn20.xml").deploy();

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

            // Start a new process instance
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("school_Admission", variables);
        }

        try {
            XesLogger.getInstance().saveLog(filePathName);
        } catch (IOException e) {
            System.out.println("Error saving file");
        }
    }
}
