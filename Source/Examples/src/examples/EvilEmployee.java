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
import ca.uqac.lif.cep.functions.IfThenElse;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.DetectEnd;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Maps;

public class EvilEmployee
{
	public static void main(String[] args)
	{
		/* Create a builder that will create events for this example. Each event is
		 * a tuple containing the employee assigned to a case, and the name of the
		 * action performed by that employee. */
		FixedTupleBuilder builder = new FixedTupleBuilder("Employee", "Action");

		/* Create a group processor that returns 1 if an event is "x", and 0
		 * otherwise. */
		GroupProcessor is_x = new GroupProcessor(1, 1);
		{
			DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
					new FetchAttribute("Action"), new Constant("END")));
			ApplyFunction eq = new ApplyFunction(new FunctionTree(Equals.instance, new FunctionTree(new FetchAttribute("Action"), StreamVariable.X), new Constant("x")));
			connect(e ,eq);
			is_x.addProcessors(e, eq).associateInput(e).associateOutput(eq);
		}
		
		Slice slice = new Slice(
				new FunctionTree(new FetchAttribute("Employee"), LogUpdate.getEvent),
				new Aggregate(is_x, Choice.INACTIVE, 
						new Cumulate(new CumulativeFunction<Boolean>(Booleans.and))));
		ApplyFunction values = new ApplyFunction(Maps.values);
		connect(slice, values);
		RunOn all = new RunOn(new Cumulate(new CumulativeFunction<Boolean>(Booleans.or)));
		connect(values, all);
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			
		}
		
		
		Pushable p = slice.getPushableInput();
		connect(all, new Println());
		p.push(new LogUpdate(0, builder.createTuple("emp0", "s")));
		p.push(new LogUpdate(0, builder.createTuple("emp0", "END")));
		p.push(new LogUpdate(0, builder.createTuple("emp1", "s")));
		p.push(new LogUpdate(0, builder.createTuple("emp1", "END")));
		
	}
}
