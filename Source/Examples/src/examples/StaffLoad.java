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

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.diagnostics.PrintThrough;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Maps.ApplyAll;
import ca.uqac.lif.cep.util.Numbers;

import static ca.uqac.lif.cep.Connector.connect;
import static ca.uqac.lif.cep.Connector.INPUT;
import static ca.uqac.lif.cep.Connector.OUTPUT;

public class StaffLoad
{

	public static void main(String[] args)
	{
		/* The number of maximum cases that can be assigned to an employee at any
		 * given moment in the execution of the system. */
		int n = 3;
		
		/* Create a builder that will create events for this example. Each event is
		 * a tuple containing the employee assigned to a case, and the name of the
		 * action performed by that employee.
		 */
		FixedTupleBuilder builder = new FixedTupleBuilder("Employee", "Action");
		
		/* Create the aggregator that counts the number of active cases assigned to
		 * an employee. This is done by transforming the events of each case into
		 * the constant 1, and summing the last event of each live trace. */
		Aggregate per_employee = new Aggregate(new TurnInto(1), Choice.ACTIVE, 
				new Cumulate(new CumulativeFunction<Number>(Numbers.addition)));
		
		
		
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			/* Split thelog by grouping traces according to the assigned employee. */
			Slice slice = new Slice(new FunctionTree(new FetchAttribute("Employee"), LogUpdate.getEvent), per_employee);
			
			/* For each value of the map, evaluate whether it is less than or equal
			 * to n. */
			ApplyFunction lte = new ApplyFunction(new FunctionTree(Maps.values,
					new ApplyAll(new FunctionTree(Numbers.isLessOrEqual,
							StreamVariable.X, new Constant(n)))));
			
			/* Assert that this condition is true for all slices by taking the
			 * conjunction of all these values. */
			RunOn and = new RunOn(new Cumulate(new CumulativeFunction<Boolean>(Booleans.and)));
			connect(slice, lte, and);
			hyperpolicy.addProcessors(slice, lte, and)
				.associateInput(INPUT, slice, INPUT).associateOutput(OUTPUT, and, OUTPUT);
		}
		
		/* Connect the hyperpolicy to processors that will print the input and
		 * output events to the console to illustrate its operation. */
		PrintThrough print = new PrintThrough();
		connect(print, hyperpolicy, new Println());
		Pushable p = print.getPushableInput();
		
		/* Push a few events to the pipeline. Here we rapidly assign 4 new cases
		 * to the same employee. */
		p.push(new LogUpdate("caseid0", builder.createTuple("emp0", "a")));
		p.push(new LogUpdate("caseid1", builder.createTuple("emp0", "a")));
		p.push(new LogUpdate("caseid2", builder.createTuple("emp0", "a")));
		p.push(new LogUpdate("caseid1", null));
		
		/* This last event violates the hyperpolicy as emp0 is assigned more than n
		 * live cases. You can try playing with these events to see the effect of
		 * modifications:
		 * - Reassigning this last event to another employee (e.g. emp1) will not
		 *   cause a violation
		 * - Closing one of the cases before (i.e. inserting a log update and
		 *   setting setting its event to null) will not cause a violation either
		 *   (as we are only concerned with open cases) */
		p.push(new LogUpdate("caseid3", builder.createTuple("emp0", "a")));

	}

}
