package hypercompliancelab.school.process.delegates;

import hypercompliancelab.school.process.pickers.Pickers;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


/***
 * Responsible for selecting an employee that handles the process
 */
public class AssignEmployeeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        // Assign the employee to the process variable
        execution.setVariable("employee", Pickers.getEmployee());
//        execution.setVariable("employee", School.getAdmissionOfficers().get(1));

    }

}
