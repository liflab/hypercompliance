/*
  A BeepBeep palette to evaluate hypercompliance queries.
  Copyright (C) 2023 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import ca.uqac.lif.cep.hypercompliance.DetectEnd;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.hypercompliance.ValueList;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Multiset;

/**
 * Evaluates the hyperpolicy stipulating that the number of <em>tasks</em>
 * assigned to an employee at any given moment must not exceed the average
 * number of tasks of all active employees by a factor <i>k</i>.
 * <p>
 * Note that in this scenario, a case is not assigned to a single employee.
 * Each individual event in the case can be arbitrarily assigned to any
 * employee.
 *  
 * @author Sylvain Hallé
 */
public class BalancedLoad
{
	public static void main(String[] args)
	{
		/* The maximum factor by which the tasks of the busiest employee can exceed
		 * the average. */
		float k = 1.5f;

		/* Create a builder that will create events for this example. Each event is
		 * a tuple containing the employee assigned to a case, and the name of the
		 * action performed by that employee. */
		FixedTupleBuilder builder = new FixedTupleBuilder("Employee", "Action");
		
		/* Create a processor that detects the end of a case, and otherwise
		 * extracts the name of the employee out of an event. */
		GroupProcessor end_emp = new GroupProcessor(1, 1);
		{
			DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
					new FetchAttribute("Action"), new Constant("END")));
			ApplyFunction get_emp = new ApplyFunction(new FetchAttribute("Employee"));
			connect(e, get_emp);
			end_emp.addProcessors(e, get_emp).associateInput(e).associateOutput(get_emp);
		}

		/* Creates the processor evaluating the hyperpolicy. */
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			/* Extract the name of the employee assigned to the last task in each
			 * active trace. */
			SliceLog slice = new SliceLog(end_emp, Choice.ACTIVE);

			/* Associate each employee with the number of tasks they are currently
			 * assigned to, and stores these values in a list. */
			ApplyFunction emp_count = new ApplyFunction(new FunctionTree(ValueList.instance, new FunctionTree(Multiset.getCardinalities, Maps.multiValues)));
			connect(slice, emp_count);

			/* Get the average and maximum of the elements in the list. */
			Fork f = new Fork();
			connect(emp_count, f);
			RunOn max = new RunOn(new Cumulate(new CumulativeFunction<Number>(Numbers.maximum)));
			RunOn avg = new RunOn(new RunningAverage());
			connect(f, 0, max, 0);
			connect(f, 1, avg, 0);

			/* Assert that the maximum is no more than n times the average. */
			ApplyFunction compare = new ApplyFunction(new FunctionTree(Numbers.isLessOrEqual, StreamVariable.X,
					new FunctionTree(Numbers.multiplication, new Constant(k), StreamVariable.Y)));
			connect(max, 0, compare, 0);
			connect(avg, 0, compare, 1);
			hyperpolicy.addProcessors(slice, emp_count, f, max, avg, compare)
				.associateInput(slice).associateOutput(compare);
		}
		
		connect(hyperpolicy, new Println());
		Pushable p = hyperpolicy.getPushableInput();
		
		/* Let's overload employee 1. */
		p.push(new LogUpdate("case0", builder.createTuple("emp0", "a")));
		p.push(new LogUpdate("case1", builder.createTuple("emp1", "a")));
		p.push(new LogUpdate("case0", builder.createTuple("emp0", "b")));
		p.push(new LogUpdate("case2", builder.createTuple("emp1", "a")));
		p.push(new LogUpdate("case0", builder.createTuple("emp0", "c")));
		p.push(new LogUpdate("case5", builder.createTuple("emp1", "a")));
		p.push(new LogUpdate("case4", builder.createTuple("emp0", "a")));
		p.push(new LogUpdate("case3", builder.createTuple("emp2", "a")));
		
		/* By assigning this event to emp1, the load of this employee now exceeds
		 * the average load by more than k. You can play with the events to see
		 * their effect on the hyperpolicy; for example:
		 * - Assigning this event to another employee does not cause a violation. 
		 */
		p.push(new LogUpdate("case0", builder.createTuple("emp1", "d")));
		
	}
}
