package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.hypercompliance.ValueList;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.*;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.Describable;
import hypercompliancelab.school.properties.helpers.RunningAverage;

import java.io.IOException;
import java.util.ArrayList;

import static ca.uqac.lif.cep.Connector.connect;

/***
 * This class implements the hyperpolicy that ensures fair distribution of applications among employees.
 *
 * author Sylvain Hallé and Chukri Soueidi
 */
public class BalancedLoad extends GroupProcessor implements Describable {

    /**
     * The name of this hyperquery.
     */
    public static final transient String NAME = "Balanced Load";

    /* The maximum factor by which the tasks of the busiest employee can exceed
     * the average. */
    public static final double K = 1.5;


    public static void main(String[] args) throws FileSystemException, IOException {
        Test.runScenario(new ArrayList<String>() {
            private static final long serialVersionUID = 1L;

            {
                add("BalancedLoad");
            }
        });
    }

    public BalancedLoad() {

        super(1, 1);



        /* Create a processor that detects the end of a case, and otherwise
         * extracts the name of the employee out of an event. */
        GroupProcessor end_emp = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
                    new FetchAttribute("state"), new Constant("END")));
            ApplyFunction get_emp = new ApplyFunction(new FetchAttribute("employee"));
            connect(e, get_emp);
            end_emp.addProcessors(e, get_emp).associateInput(e).associateOutput(get_emp);
        }

        /* Creates the processor evaluating the hyperpolicy. */
        GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
        {
            /* Extract the name of the employee assigned to the last task in each
             * active trace. */
            SliceLog slice = new SliceLog(end_emp, SliceLog.Choice.ACTIVE);

            /* Associate each employee with the number of tasks they are currently
             * assigned to, and stores these values in a list. */
            ApplyFunction emp_count = new ApplyFunction(new FunctionTree(ValueList.instance, new FunctionTree(Multiset.getCardinalities, Maps.multiValues)));
            connect(slice, emp_count);

            /* Get the average and maximum of the elements in the list. */
            Fork f = new Fork();
            connect(emp_count, f);
            Bags.RunOn max = new Bags.RunOn(new Cumulate(new CumulativeFunction<Number>(Numbers.maximum)));
            Bags.RunOn avg = new Bags.RunOn(new RunningAverage());
            connect(f, 0, max, 0);
            connect(f, 1, avg, 0);

            /* Assert that the maximum is no more than n times the average. */
            ApplyFunction compare = new ApplyFunction(new FunctionTree(Numbers.isLessOrEqual, StreamVariable.X,
                    new FunctionTree(Numbers.multiplication, new Constant(K), StreamVariable.Y)));
            connect(max, 0, compare, 0);
            connect(avg, 0, compare, 1);
            hyperpolicy.addProcessors(slice, emp_count, f, max, avg, compare)
                    .associateInput(slice).associateOutput(compare);
        }

        addProcessors(hyperpolicy).associateInput(hyperpolicy).associateOutput(hyperpolicy);
        ;
    }

    @Override
    public String getDescription() {
        return "An even allocation of applications across employee";
    }
}
