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
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.ltl.HardCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Multiset;

/**
 * Evaluates the hyperpolicy that stipulates that no more than <i>k</i>
 * instances of a process can be in the same state at the same time. Each event
 * in a trace is taken to be the current state of the process.
 * 
 * @author Sylvain Hallé
 * @see SameStateMoore
 */
public class SameStateDirect
{
	public static void main(String[] args)
	{
		/* The maximum number of instances that can be in the same state at the
		 * same time. */
		int k = 2;
		
		/* Create the hyperpolicy. */
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			/* Slice the log according to the trace identifier. */
			SliceLog slice = new SliceLog(new Passthrough());
			
			/* Group the output of the slice according to the number of instances
			 * being in a given state, and put these values into a set. */
			ApplyFunction vals = new ApplyFunction(new FunctionTree(
					Maps.values, new FunctionTree(Multiset.getCardinalities, Maps.multiValues)));
			connect(slice, vals);
			
			/* Get the maximum value of the set. */
			RunOn ro = new RunOn(new Cumulate(new CumulativeFunction<Number>(Numbers.maximum)));
			connect(vals, ro);
			
			/* Assert that this maximum is less than or equal to k. */
			Fork f = new Fork();
			connect(ro, f);
			ApplyFunction lte = new ApplyFunction(new FunctionTree(HardCast.instance, Numbers.isLessOrEqual));
			connect(f, 0, lte, 0);
			TurnInto cons = new TurnInto(k);
			connect(f, 1, cons, 0);
			connect(cons, 0, lte, 1);
			hyperpolicy.addProcessors(slice, vals, ro, f, lte, cons)
				.associateInput(slice).associateOutput(lte);
		}
		
		connect(hyperpolicy, new Println());
		Pushable p = hyperpolicy.getPushableInput();
		
		/* Push a few events to illustrate the operation. */
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(2, "a"));
		
		/* This last event causes a violation, as 3 processes are currently in
		 * state a.*/
		p.push(new LogUpdate(3, "a"));
		
		
	}
}
