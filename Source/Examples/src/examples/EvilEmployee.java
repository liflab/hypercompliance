/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2023 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package examples;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.DetectEnd;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.hypercompliance.Weaken;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.ltl.SoftCast;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.ltl.TrooleanCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Evaluates the hyperpolicy that stipulates that no employee has all its
 * associated cases ending in state "x". Here "x" designates, for example, a
 * failure or a rejection.
 *  
 * @author Sylvain Hallé
 *
 */
public class EvilEmployee
{
	public static void main(String[] args)
	{
		int k = 0;
		
		/* Create a builder that will create events for this example. Each event is
		 * a tuple containing the employee assigned to a case, and the name of the
		 * action performed by that employee. */
		FixedTupleBuilder builder = new FixedTupleBuilder("Employee", "Action");

		/* Create a group processor that returns 1 if an event is "x", and 0
		 * otherwise. */
		GroupProcessor is_not_x = new GroupProcessor(1, 1);
		{
			DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
					new FetchAttribute("Action"), new Constant("END")));
			ApplyFunction eq = new ApplyFunction(new FunctionTree(TrooleanCast.instance, new FunctionTree(Booleans.not, new FunctionTree(Equals.instance, new FunctionTree(new FetchAttribute("Action"), StreamVariable.X), new Constant("x")))));
			connect(e ,eq);
			is_not_x.addProcessors(e, eq).associateInput(e).associateOutput(eq);
		}
		
		GroupProcessor enough_cases = new GroupProcessor(1, 1);
		{
			Aggregate agg = new Aggregate(new TurnInto(1), Choice.INACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), new Object[] {0});
			Fork f = new Fork();
			connect(agg, f);
			ApplyFunction gte = new ApplyFunction(new FunctionTree(TrooleanCast.instance, Numbers.isGreaterOrEqual));
			connect(f, 0, gte, 0);
			TurnInto to_k = new TurnInto(k);
			connect(f, 1, to_k, 0);
			connect(to_k, 0, gte, 1);
			enough_cases.addProcessors(agg, f, gte, to_k).associateInput(agg).associateOutput(gte);
		}
		//TurnInto enough_cases = new TurnInto(Troolean.Value.TRUE);
		
		/* Create the hyperpolicy. */
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			/* Slice the input stream according to each employee, and then according
			 * to the trace identifier. For all completed traces, check if it ends
			 * in state "x", and take the conjunction of this condition evaluated on
			 * all completed traces. */
			Slice slice = new Slice(
					new FunctionTree(new FetchAttribute("Employee"), LogUpdate.getEvent),
					new Weaken(
							enough_cases,
							new Aggregate(is_not_x, Choice.INACTIVE, 
									new Cumulate(new CumulativeFunction<Troolean.Value>(Troolean.OR_FUNCTION)), new Object[] {Troolean.Value.INCONCLUSIVE}))
					);
			
			/* The resulting output is a map that associates employees with a Boolean
			 * verdict. Take the set of all such Boolean values and calculate their
			 * disjunction. */
			ApplyFunction values = new ApplyFunction(Maps.values);
			connect(slice, values);
			RunOn all = new RunOn(new Cumulate(new CumulativeFunction<Troolean.Value>(Troolean.AND_FUNCTION)));
			connect(values, all);
			//ApplyFunction not = new ApplyFunction(Troolean.NOT_FUNCTION);
			//connect(all, not);
			/* The end result is a condition that returns false as soon
			 * as one employee has all its inactive traces ending in "x". */
			hyperpolicy.addProcessors(slice, values, all)
				.associateInput(slice).associateOutput(all);
		}
		
		/* Push a few events to illustrate the operation. */
		Pushable p = hyperpolicy.getPushableInput();
		connect(hyperpolicy, new Println());
		p.push(new LogUpdate(0, builder.createTuple("emp0", "x")));
		p.push(new LogUpdate(0, builder.createTuple("emp0", "END")));
		p.push(new LogUpdate(0, builder.createTuple("emp1", "a")));
		p.push(new LogUpdate(0, builder.createTuple("emp1", "END")));
	}
}
