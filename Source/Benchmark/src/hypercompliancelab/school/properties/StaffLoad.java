package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.hypercompliance.Weaken;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.ltl.TrooleanCast;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.*;
import hypercompliancelab.Describable;

import java.util.ArrayList;

import static ca.uqac.lif.cep.Connector.connect;

/**
 * Evaluates the hyperpolicy stipulating that at any moment, the number of
 * active cases assigned to an employee must not exceed a constant <i>n</i>.
 *
 * @author Sylvain Hallé and Chukri Soueidi
 */
public class StaffLoad extends GroupProcessor implements Describable {

    /**
     * The name of this hyperquery.
     */
    public static final transient String NAME = "Employee Capacity Limit";

    /**
     * The number of cases that an employee can handle at any given time.
     */
    protected static final int MAX_CASES_PER_EMPLOYEE = 3;


    public static void main(String[] args) {
        Test.runScenario(new ArrayList<String>() {
            {
                add("StaffLoad");
            }
        });
    }

    public StaffLoad() {

        super(1, 1);



        /* Create a processor that detects the end of a slice. A slice is finished
         * when an event whose action is "END" is seen. When this event occurs,
         * the DetectEnd processor sends the end of trace signal to indicate that
         * the slice is over. */
        GroupProcessor end_to_one = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
            TurnInto one = new TurnInto(1);
            connect(e, one);
            end_to_one.addProcessors(e, one).associateInput(e).associateOutput(one);
        }


        /* Create the aggregator that counts the number of active cases assigned to
         * an employee. This is done by transforming the events of each case into
         * the constant 1, and summing the last event of each live trace. */
        Aggregate per_employee = new Aggregate(end_to_one, SliceLog.Choice.ACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)));

        GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
        {
            /* Split the log by grouping traces according to the assigned employee. */
            Slice slice = new Slice(new FunctionTree(new FetchAttribute("employee"), LogUpdate.getEvent), per_employee);

            /* For each value of the map, evaluate whether it is less than or equal
             * to n. */
            ApplyFunction lte = new ApplyFunction(new FunctionTree(Maps.values, new Maps.ApplyAll(new FunctionTree(Numbers.isLessOrEqual, StreamVariable.X, new Constant(MAX_CASES_PER_EMPLOYEE)))));

            /* Assert that this condition is true for all slices by taking the
             * conjunction of all these values. */
            Bags.RunOn and = new Bags.RunOn(new Cumulate(new CumulativeFunction<Boolean>(Booleans.and)));
            connect(slice, lte, and);
            hyperpolicy.addProcessors(slice, lte, and).associateInput(slice).associateOutput(and);
        }

        addProcessors(hyperpolicy).associateInput(hyperpolicy).associateOutput(hyperpolicy);
        ;
    }

    @Override
    public String getDescription() {
        return "Restricts the total number of applicants assigned to an employee at a given time to 3.";
    }
}
