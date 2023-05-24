package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.hypercompliance.ValueList;
import ca.uqac.lif.cep.ltl.HardCast;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.*;
import hypercompliancelab.Describable;

import java.util.ArrayList;

import static ca.uqac.lif.cep.Connector.connect;

/**
 * Evaluates the hyperpolicy that stipulates that no more than <i>k</i>
 * instances of a process can be in the same state at the same time. Each event
 * in a trace is taken to be the current state of the process.
 *
 * @author Sylvain Hallé and Chukri
 */
public class SameState extends GroupProcessor implements Describable {

    /**
     * The name of this hyperquery.
     */
    public static final transient String NAME = "Same State (Under Review)";

    /* The maximum number of instances of a process that can be in the same
     * state at the same time. */
    public static final double K = 5;


    public static void main(String[] args) {
        Test.runScenario(new ArrayList<String>() {
            {
                add("SameState");
            }
        });
    }


    public SameState() {

        super(1, 1);

        GroupProcessor end_emp = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
            ApplyFunction get_emp = new ApplyFunction(new FetchAttribute("state"));
            connect(e, get_emp);
            end_emp.addProcessors(e, get_emp).associateInput(e).associateOutput(get_emp);
        }

        GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
        {

            SliceLog slice = new SliceLog(end_emp, SliceLog.Choice.ACTIVE);

            ApplyFunction vals = new ApplyFunction(new FunctionTree(Maps.values, new FunctionTree(Multiset.getCardinalities, Maps.multiValues)));
            connect(slice, vals);

            /* Get the maximum value of the set. */
            Bags.RunOn ro = new Bags.RunOn(new Cumulate(new CumulativeFunction<Number>(Numbers.maximum)));
            connect(vals, ro);


            /* Assert that this maximum is less than or equal to k. */
            Fork f = new Fork();
            connect(ro, f);
            ApplyFunction lte = new ApplyFunction(new FunctionTree(HardCast.instance, Numbers.isLessOrEqual));
            connect(f, 0, lte, 0);
            TurnInto cons = new TurnInto(K);
            connect(f, 1, cons, 0);
            connect(cons, 0, lte, 1);

            hyperpolicy.addProcessors(slice, end_emp, vals, ro, f, lte, cons).associateInput(slice).associateOutput(lte);
        }

        addProcessors(hyperpolicy).associateInput(hyperpolicy).associateOutput(hyperpolicy);
        ;
    }

    @Override
    public String getDescription() {
        return "No more than n live applications are in the same state simultaneously";
    }
}
