package hypercompliancelab.school.properties;


import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.ltl.HardCast;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.*;

import java.util.ArrayList;

import static ca.uqac.lif.cep.Connector.connect;

/**
 * Evaluates the hyperpolicy stipulating that at any moment, the number of
 * active cases assigned to an employee must not exceed a constant <i>n</i>.
 *
 * @author Sylvain Hallé
 */

@Deprecated
public class AllEndedChecker {

    public static void main(String[] args) {

        Test.runScenario(new ArrayList<String>() {
            {
                add("AllEndedChecker");
            }
        });
    }

    public Processor getHyperpolicy() {

        int k = 5;

        GroupProcessor end_emp = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
            ApplyFunction get_emp = new ApplyFunction(new FetchAttribute("state"));
            connect(e, get_emp);
            end_emp.addProcessors(e, get_emp).associateInput(e).associateOutput(get_emp);
        }

        GroupProcessor agg = new GroupProcessor(1, 1);
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
            TurnInto cons = new TurnInto(k);
            connect(f, 1, cons, 0);
            connect(cons, 0, lte, 1);

            agg.addProcessors(slice, end_emp, vals, ro, f, lte, cons).associateInput(slice).associateOutput(lte);
        }

//        /* Instantiate a processor expressing a condition that the traces
//         * must satisfy to be considered in the hyperpolicy. */
//        Processor condition = new Sometime(new ApplyFunction(new FunctionTree(TrooleanCast.instance,
//                new FunctionTree(Equals.instance, new FetchAttribute("employee"), new Constant("Admissions Officer 9")))));
//
//        /* Create a trace processor that calculates the end state of traces
//         * that satisfy the condition. */
//        GroupProcessor last_state = new GroupProcessor(1, 1);
//        {
//            MonitorFilter filter = new MonitorFilter(condition);
//
//            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance, new FetchAttribute("state"), new Constant("END")));
//            connect(filter, e);
//            KeepLast last = new KeepLast();
//            connect(e, last);
//            last_state.addProcessors(filter, e, last).associateInput(0, filter, 0).associateOutput(0, last, 0);
//        }
//
//        Aggregate agg = new Aggregate(last_state, SliceLog.Choice.ALL, new Sets.PutInto());

//        int n = 3;
//
//        /* Create a processor that detects the end of a slice. A slice is finished
//         * when an event whose action is "END" is seen. When this event occurs,
//         * the DetectEnd processor sends the end of trace signal to indicate that
//         * the slice is over. */
//        GroupProcessor end_to_one = new GroupProcessor(1, 1);
//        {
//            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
//                        new FetchAttribute("state"), new Constant("END")));
//            TurnInto one = new TurnInto(1);
//            connect(e, one);
//            end_to_one.addProcessors(e, one).associateInput(e).associateOutput(one);
//
////            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
////                    new FetchAttribute("state"), new Constant("END")));
////            TurnInto one = new TurnInto(1);
////            connect(e, one);
////            end_to_one.addProcessors(e, one).associateInput(e).associateOutput(one);
//        }
//
//        Aggregate agg = new Aggregate(end_to_one, SliceLog.Choice.ACTIVE,
//                new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), new Object[] {0});

        return agg;

//        /* Create the aggregator that counts the number of active cases assigned to
//         * an employee. This is done by transforming the events of each case into
//         * the constant 1, and summing the last event of each live trace. */
//        Aggregate per_employee = new Aggregate(end_to_one, SliceLog.Choice.ACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)));
//
//        GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
//        {
//            /* Split the log by grouping traces according to the assigned employee. */
//            Slice slice = new Slice(new FunctionTree(new FetchAttribute("employee"), LogUpdate.getEvent), per_employee);
//
//            /* For each value of the map, evaluate whether it is less than or equal
//             * to n. */
//            ApplyFunction lte = new ApplyFunction(new FunctionTree(Maps.values, new Maps.ApplyAll(new FunctionTree(Numbers.isLessOrEqual, StreamVariable.X, new Constant(n)))));
//
//            /* Assert that this condition is true for all slices by taking the
//             * conjunction of all these values. */
//            Bags.RunOn and = new Bags.RunOn(new Cumulate(new CumulativeFunction<Boolean>(Booleans.and)));
//            connect(slice, lte, and);
//            hyperpolicy.addProcessors(slice, lte, and).associateInput(slice).associateOutput(and);
//        }
//
//        return hyperpolicy;
    }
}
