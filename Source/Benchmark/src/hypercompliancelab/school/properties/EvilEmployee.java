package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
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

import static ca.uqac.lif.cep.Connector.connect;

/***
 * This class implements the hyperpolicy that checks whether employees are rejecting all applications.
 *
 * @author Sylvain Hallé and Chukri Soueidi
 */
public class EvilEmployee extends GroupProcessor implements Describable {

    /**
     * The name of this hyperquery.
     */
    public static final transient String NAME = "Evil Employee";


    public EvilEmployee() {

        super(1, 1);



        /* The minimum number of cases that an employee must have completed in
         * order to be considered in the hyperpolicy. */
        int k = 10;



        /* Create a group processor that returns 1 if an event is "x", and 0
         * otherwise. */
        GroupProcessor is_not_rejected = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
            ApplyFunction eq = new ApplyFunction(new FunctionTree(TrooleanCast.instance, new FunctionTree(Booleans.not, new FunctionTree(Equals.instance, new FunctionTree(new FetchAttribute("state"), StreamVariable.X), new Constant("Rejected")))));
            connect(e, eq);
            is_not_rejected.addProcessors(e, eq).associateInput(e).associateOutput(eq);
        }

        GroupProcessor one_end = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
            TurnInto one = new TurnInto(1);
            connect(e, one);
            one_end.addProcessors(e, one).associateInput(e).associateOutput(one);
        }

        /* Create a processor evaluating the condition that employees have at
         * least k inactive (i.e. completed) traces associated to them. This
         * condition is used to weaken the hyperpolicy, by avoiding alarms raised
         * by employees with only few cases. */
        GroupProcessor enough_cases = new GroupProcessor(1, 1);
        {
            Aggregate agg = new Aggregate(one_end, SliceLog.Choice.INACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), new Object[]{0});
            Fork f = new Fork();
            connect(agg, f);
            ApplyFunction gte = new ApplyFunction(new FunctionTree(TrooleanCast.instance, Numbers.isGreaterOrEqual));
            connect(f, 0, gte, 0);
            TurnInto to_k = new TurnInto(k);
            connect(f, 1, to_k, 0);
            connect(to_k, 0, gte, 1);
            enough_cases.addProcessors(agg, f, gte, to_k).associateInput(agg).associateOutput(gte);
        }



        /* Create the hyperpolicy. */
        GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
        {
            /* Slice the input stream according to each employee, and then according
             * to the trace identifier. For all completed traces, check if at least
             * one of them does not end  in state "x", and take the disjunction of
             * this condition evaluated on all completed traces. */
            Slice slice = new Slice(new FunctionTree(new FetchAttribute("employee"), LogUpdate.getEvent), new Weaken(enough_cases, new Aggregate(is_not_rejected, //processor
                    SliceLog.Choice.INACTIVE, //choice
                    new Cumulate(new CumulativeFunction<Troolean.Value>(Troolean.OR_FUNCTION)), //aggregator
                    new Object[]{Troolean.Value.INCONCLUSIVE}) //default values
            ));

            /* The resulting output is a map that associates employees with a Boolean
             * verdict. Take the set of all such Boolean values and calculate their
             * disjunction. */
            ApplyFunction values = new ApplyFunction(Maps.values);
            connect(slice, values);
            Bags.RunOn all = new Bags.RunOn(new Cumulate(new CumulativeFunction<Troolean.Value>(Troolean.AND_FUNCTION)));
            connect(values, all);

            /* The end result is a condition that returns false as soon
             * as one employee has all its inactive traces ending in "x". */
            hyperpolicy.addProcessors(slice, values, all).associateInput(slice).associateOutput(all);
        }

        addProcessors(hyperpolicy).associateInput(hyperpolicy).associateOutput(hyperpolicy);
        ;
    }

    @Override
    public String getDescription() {
        return "Ensures no employee rejects all assigned applications.";
    }
}
