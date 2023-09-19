package hypercompliancelab.school.process.logging;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.FixedValue;

import hypercompliancelab.school.process.pickers.TimestampGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class logs an event on the execution of each task in the process in XES format.
 */
public class XesExecutionListener implements ExecutionListener {
    private static final long serialVersionUID = 1L;


    private final XesLogger xesLogger;


    public FixedValue state;

    public XesExecutionListener() {
        this.xesLogger = XesLogger.getInstance();

    }


    public static HashSet<String> seenVariables = new HashSet<>();


    /***
     * This method is called when a task is executed. It creates a new trace if the process instance is new. Else it logs an event in the existing trace.
     * Variables are logged as attributes of the event. Variables starting with x_ are not logged, they are used for internal purposes. Variables starting with t_ are temporary variables and are only logged once.
     * Other variables are logged.
     * @param execution
     */
    @Override
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();

        if (isNewInstance(execution)) {

            xesLogger.createNewTrace(processInstanceId);
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("timestamp", TimestampGenerator.generate(processInstanceId));
        execution.getVariables().entrySet().stream().filter(v -> !v.getKey().startsWith("_")).forEach(v -> {
            if (v.getKey().startsWith("t_")) {
                //temp variable show only once in log
                if (seenVariables.contains(v.getKey() + processInstanceId)) {

                } else {
                    seenVariables.add(v.getKey() + processInstanceId);
                    attributes.put(v.getKey().replaceFirst("t_", ""), v.getValue().toString());
                }
            } else if (!v.getKey().startsWith("x_")) {
                attributes.put(v.getKey(), v.getValue().toString());
            }

        });

        Expression resultVarExpression = DelegateHelper.getFieldExpression(execution, "state");
        if (resultVarExpression != null) {
            attributes.put("state", resultVarExpression.getExpressionText());
        }

        xesLogger.appendEvent(processInstanceId, attributes);

        // Closing the trace is not handled somewhere else

    }


    private boolean isNewInstance(DelegateExecution execution) {
        // Check if the current execution is a start event
        return "start".equals(execution.getEventName()) && execution.getCurrentActivityId().equals("startEvent");
    }

    private boolean isEndEvent(DelegateExecution execution) {
        // Check if the current execution is an end event
        return "end".equals(execution.getEventName());
    }
}
