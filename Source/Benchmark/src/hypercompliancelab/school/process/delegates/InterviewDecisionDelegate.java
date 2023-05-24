package hypercompliancelab.school.process.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import hypercompliancelab.school.process.pickers.Pickers;

/***
 * Responsible for picking a decision after the interview.
 */
public class InterviewDecisionDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        // Assign the employee to the process variable
        execution.setVariable("x_acceptance_Decision", Pickers.getAcceptanceDecision(execution.getVariable("employee"), execution.getVariable("applicant")));


    }

}
