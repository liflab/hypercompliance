package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.MonitorFilter;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.ltl.Sometime;
import ca.uqac.lif.cep.ltl.TrooleanCast;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Sets;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.Describable;

import java.io.IOException;
import java.util.ArrayList;

import static ca.uqac.lif.cep.Connector.connect;


/**
 * Evaluates the hyperpolicy that stipulates that every trace that contains a lowGPA must end in the same state.
 * @author Sylvain Hallé and Chukri Soueidi
 */


public class ConsistencyCondition extends GroupProcessor implements Describable {
    /**
     * The name of this hyperquery.
     */
    public static final transient String NAME = "Consistency Condition";

    public static void main(String[] args) throws FileSystemException, IOException {
        Test.runScenario(new ArrayList<String>() {
            {
                add("ConsistencyCondition");
            }
        });
    }


    public ConsistencyCondition() {

        super(1, 1);

        /* Instantiate a processor expressing a condition that the traces
         * must satisfy to be considered in the hyperpolicy. */
        Processor condition = new Sometime(new ApplyFunction(new FunctionTree(TrooleanCast.instance,
                new FunctionTree(Equals.instance, new FetchAttribute("lowGPA"), new Constant("true")))));


        /* Create a trace processor that calculates the end state of traces
         * that satisfy the condition. */
        GroupProcessor last_state = new GroupProcessor(1, 1);
        {
            MonitorFilter filter = new MonitorFilter(condition);

            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
            connect(filter, e);
            KeepLast last = new KeepLast();
            connect(e, last);
            last_state.addProcessors(filter, e, last).associateInput(0, filter, 0).associateOutput(0, last, 0);
        }

        /* Create the hyperpolicy. */
        GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
        {
            Aggregate agg = new Aggregate(last_state, SliceLog.Choice.ALL, new Sets.PutInto());
            Fork f = new Fork();
            connect(agg, f);
            ApplyFunction size = new ApplyFunction(Bags.getSize);
            connect(f, 0, size, 0);
            TurnInto one = new TurnInto(1);
            connect(f, 1, one, 0);
            ApplyFunction geq = new ApplyFunction(Numbers.isLessOrEqual);
            connect(size, 0, geq, 0);
            connect(one, 0, geq, 1);
            hyperpolicy.addProcessors(agg, f, size, one, geq).associateInput(0, agg, 0).associateOutput(0, geq, 0);
        }
        addProcessors(hyperpolicy).associateInput(hyperpolicy).associateOutput(hyperpolicy);;
    }


        @Override

    public String getDescription() {

        return "Ensures consistency in admitting students with low GPAs.";
    }

}
