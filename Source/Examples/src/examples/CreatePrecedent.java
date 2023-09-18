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
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.hypercompliance.AggregateSequence;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Freeze;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.NthElement;
import examples.MonotonicLength.Counter;

/**
 * Evaluates the hyperpolicy that stipulates that whenever a
 * <em>completed</em> trace is seen that satisfies both &phi; and &psi;, any
 * <em>completed</em> trace that started at a later time and which satisfies
 * &phi; must also satisfy &psi;. Stated simply, the occurrence of &psi; is a
 * form of "precedent" for a trace that satisfies &phi;, and this precedent
 * dictates what must happen to other traces that satisfy &phi; from that
 * point on.
 * <p>
 * In this simple example, &phi; is the condition "a has occurred at most
 * once", and &psi; is the condition "b has occurred at most once". Of course,
 * concretely &phi; and &psi; are two arbitrary processors, which could be
 * replaced with more complex conditions on the sequence of events in the
 * trace.
 * <p>
 * This example makes use of the {@link AggregateSequence} processor, which in
 * this case orders slices according to the moment they start.
 * 
 * @author Sylvain Hallé
 */
public class CreatePrecedent
{
	public static void main(String[] args)
	{
		AggregateSequence ags = new AggregateSequence(
			new GroupProcessor(1, 1) {{
				DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant("END")));
				Fork f = new Fork();
				connect(end, f);
				SeenSymbol seen_a = new SeenSymbol("a");
				connect(f, 0, seen_a, 0);
				SeenSymbol seen_b = new SeenSymbol("b");
				connect(f, 1, seen_b, 0);
				Fork f_a = new Fork();
				connect(seen_a, f_a);
				Fork f_b = new Fork();
				connect(seen_b, f_b);
				ApplyFunction a_and_b = new ApplyFunction(Booleans.and);
				connect(f_a, 0, a_and_b, 0);
				connect(f_b, 0, a_and_b, 1);
				ApplyFunction a_imp_b = new ApplyFunction(Booleans.implies);
				connect(f_a, 1, a_imp_b, 0);
				connect(f_b, 1, a_imp_b, 1);
				ApplyFunction to_array = new ApplyFunction(new Bags.ToArray(2));
				connect(a_and_b, 0, to_array, 0);
				connect(a_imp_b, 0, to_array, 1);
				addProcessors(end, f, seen_a, seen_b, f_a, f_b, a_and_b, a_imp_b, to_array);
				associateInput(end).associateOutput(to_array);
			}},
			Choice.INACTIVE,
			new Counter(),
			new Freeze(),
			new GroupProcessor(1, 1) {{
				Fork f = new Fork();
				ApplyFunction phi = new ApplyFunction(new FunctionTree(Booleans.booleanCast, new NthElement(0)));
				connect(f, 0, phi, 0);
				Cumulate phi_op = new Cumulate(Booleans.or);
				connect(phi, phi_op);
				ApplyFunction psi = new ApplyFunction(new FunctionTree(Booleans.booleanCast, new NthElement(1)));
				connect(f, 1, psi, 0);
				ApplyFunction implies = new ApplyFunction(Booleans.implies);
				connect(phi_op, 0, implies, 0);
				connect(psi, 0, implies, 1);
				Cumulate g = new Cumulate(Booleans.and);
				connect(implies, g);
				addProcessors(f, phi, phi_op, psi, implies, g);
				associateInput(f).associateOutput(g);
			}}
		);
		connect(ags, new Println());
		Pushable p = ags.getPushableInput();
		
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(0, "c"));
		p.push(new LogUpdate(0, "END"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(1, "END"));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(2, "END"));
		p.push(new LogUpdate(3, "a"));
		p.push(new LogUpdate(3, "c"));
		p.push(new LogUpdate(3, "END"));
	}
	
	protected static class SeenSymbol extends GroupProcessor
	{
		public SeenSymbol(String x)
		{
			super(1, 1);
			ApplyFunction has_a = new ApplyFunction(new FunctionTree(Equals.instance, StreamVariable.X, new Constant(x)));
			Cumulate or = new Cumulate(Booleans.or);
			connect(has_a, or);
			addProcessors(has_a, or);
			associateInput(has_a).associateOutput(or);
		}
	}
}
