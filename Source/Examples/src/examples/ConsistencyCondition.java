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
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.diagnostics.PrintThrough;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.MonitorFilter;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.ltl.Sometime;
import ca.uqac.lif.cep.ltl.TrooleanCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Sets;

import static ca.uqac.lif.cep.Connector.connect;

public class ConsistencyCondition
{
	public static void main(String[] args)
	{
		/* Instantiate a processor expressing a condition that the traces
		 * must satisfy to be considered in the hyperpolicy. */
		Processor condition = new Sometime(new ApplyFunction(new FunctionTree(TrooleanCast.instance, new FunctionTree(Equals.instance, StreamVariable.X, new Constant("a")))));
		
		/* Create a trace processor that calculates the end state of traces
		 * that satisfy the condition. */
		GroupProcessor last_state = new GroupProcessor(1, 1);
		{
			MonitorFilter filter = new MonitorFilter(condition);
			KeepLast last = new KeepLast();
			connect(filter, last);
			last_state.addProcessors(filter, last)
				.associateInput(0, filter, 0)
				.associateOutput(0, last, 0);
		}
		
		/* Create the hyperpolicy. */
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			Aggregate agg = new Aggregate(last_state, Choice.ALL, new Sets.PutInto());
			Fork f = new Fork();
			connect(agg, f);
			ApplyFunction size = new ApplyFunction(Bags.getSize);
			connect(f, 0, size, 0);
			TurnInto one = new TurnInto(1);
			connect(f, 1, one, 0);
			ApplyFunction geq = new ApplyFunction(Numbers.isLessOrEqual);
			connect(size, 0, geq, 0);
			connect(one, 0, geq, 1);
			hyperpolicy.addProcessors(agg, f, size, one, geq)
				.associateInput(0, agg, 0).associateOutput(0, geq, 0);
		}
		
		/* Instantiate a PrintThrough processor that will print each
		 * event pushed to the pipeline. This will make it easier to see the
		 * relationship between input and output events. */
		PrintThrough pt = new PrintThrough();
		connect(pt, hyperpolicy, new Println());
		
		/* Push events to the pipeline. */
		Pushable p = pt.getPushableInput();
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(0, null));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(1, "c"));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(2, null));
		
		/* This last event should cause the policy to be violated: trace 1 contains
		 * an a, but ends in a different state as the others. */
		p.push(new LogUpdate(1, null));
	}
}
