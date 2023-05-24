package hypercompliancelab.school.process.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import hypercompliancelab.school.process.pickers.Pickers;

/***
 * Responsible for picking an interview date for the interviews
 */
public class InterviewDateDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        // Assign the employee to the process variable
        execution.setVariable("t_interview_date", Pickers.getInterviewDate());


    }

}
