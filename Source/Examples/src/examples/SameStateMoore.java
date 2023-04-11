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
import ca.uqac.lif.cep.fsm.FunctionTransition;
import ca.uqac.lif.cep.fsm.MooreMachine;
import ca.uqac.lif.cep.fsm.TransitionOtherwise;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.ltl.HardCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Multiset;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Bags.RunOn;

public class SameStateMoore
{
	public static void main(String[] args)
	{
		int k = 2;
		
		MooreMachine m = new MooreMachine(1, 1);
		{
			m.addTransition(0, getTransition("a", 1));
			m.addTransition(0, new TransitionOtherwise(0));
			m.addTransition(1, getTransition("a", 0));
			m.addTransition(1, getTransition("b", 2));
			m.addTransition(1, getTransition("c", 3));
			m.addTransition(2, getTransition("a", 4));
			m.addTransition(2, getTransition("b", 0));
			m.addTransition(2, getTransition("c", 2));
			m.addTransition(3, getTransition("c", 1));
			m.addTransition(3, new TransitionOtherwise(3));
			m.addTransition(4, new TransitionOtherwise(3));
			m.addSymbol(0, new Constant(0));
			m.addSymbol(1, new Constant(1));
			m.addSymbol(2, new Constant(2));
			m.addSymbol(3, new Constant(3));
			m.addSymbol(4, new Constant(4));
		}
		
		GroupProcessor hyperpolicy = new GroupProcessor(1, 1);
		{
			SliceLog slice = new SliceLog(m);
			ApplyFunction vals = new ApplyFunction(new FunctionTree(Maps.values, new FunctionTree(Multiset.getCardinalities, Maps.multiValues)));
			connect(slice, vals);
			RunOn ro = new RunOn(new Cumulate(new CumulativeFunction<Number>(Numbers.maximum)));
			connect(vals, ro);
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
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(0, "c"));
		p.push(new LogUpdate(3, "a"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(3, "c"));
		p.push(new LogUpdate(2, "b"));
		p.push(new LogUpdate(1, "a"));
	}
	
	protected static FunctionTransition getTransition(String label, int destination)
	{
		return new FunctionTransition(new FunctionTree(Equals.instance, StreamVariable.X, new Constant(label)), destination);
	}
}
