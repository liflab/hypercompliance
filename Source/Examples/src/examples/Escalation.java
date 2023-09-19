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
import ca.uqac.lif.cep.functions.IfThenElse;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.hypercompliance.AggregateSequence;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Freeze;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.FixedTupleBuilder;
import ca.uqac.lif.cep.util.Bags.ToArray;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.NthElement;
import ca.uqac.lif.cep.util.Numbers;
import examples.MonotonicLength.Counter;

/**
 * Evaluates the hyperpolicy stipulating that if a client has <i>n</i> of his
 * applications ending in a rejection, the next application for this client
 * must be assigned to the manager. Graphically, this pipeline can be
 * represented as follows:
 * <p>
 * <img src="{@docRoot}/doc-files/Escalation.png" alt="Pipeline"/>
 */
public class Escalation
{
  /**
   * The string designating the manager.
   */
  public static final String MANAGER = "manager";
  
  /**
   * The string designating the end of a slice.
   */
  public static final String END = "END";
  
	public static void main(String[] args)
	{
		/* The number of rejected applications for a client before it is assigned
		 * to the manager. */
		int k = 2;
		
		/* Create a builder that will create events for this example. Each event is
		 * a tuple containing the name of the client for that case, the name of the
		 * employee assigned to that case, and the name of the action performed by
		 * the employee. */
		FixedTupleBuilder builder = new FixedTupleBuilder("Client", "Employee", "Action");
		
		Slice by_emp = new Slice(new FunctionTree(new FetchAttribute("Client"), LogUpdate.getEvent),
				new AggregateSequence(
						new GroupProcessor(1, 1) {{
							DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant(END)));
							Fork f = new Fork();
							connect(end, f);
							ApplyFunction is_failure = new ApplyFunction(new FunctionTree(Equals.instance, new FunctionTree(new FetchAttribute("Action"), StreamVariable.X), new Constant("X")));
							connect(f, 0, is_failure, 0);
							Cumulate or = new Cumulate(Booleans.or);
							connect(is_failure, or);
							ApplyFunction get_emp = new ApplyFunction(new FetchAttribute("Employee"));
							connect(f, 1, get_emp, 0);
							Freeze fr = new Freeze();
							connect(get_emp, fr);
							ApplyFunction merge = new ApplyFunction(new ToArray(2));
							connect(or, 0, merge, 0);
							connect(fr, 0, merge, 1);
							addProcessors(f, end, is_failure, or, get_emp, fr, merge);
							associateInput(end).associateOutput(merge);
						}},
				Choice.ALL,
				new Counter(),
				new KeepLast(),
				new GroupProcessor(1, 1) {{ 
					Fork f = new Fork();
					ApplyFunction is_failure = new ApplyFunction(new FunctionTree(IfThenElse.instance, new FunctionTree(new NthElement(0), StreamVariable.X), new Constant(1), new Constant(0)));
					connect(f, 0, is_failure, 0);
					Cumulate add = new Cumulate(Numbers.addition);
					connect(is_failure, add);
					ApplyFunction gt = new ApplyFunction(new FunctionTree(Numbers.isGreaterOrEqual, StreamVariable.X, new Constant(k)));
					connect(add, gt);
					Trim trim = new Trim(1);
					connect(f, 1, trim, 0);
					ApplyFunction is_man = new ApplyFunction(new FunctionTree(Equals.instance, new FunctionTree(new NthElement(1), StreamVariable.X), new Constant(MANAGER)));
					connect(trim, is_man);
					ApplyFunction implies = new ApplyFunction(Booleans.implies);
					connect(gt, 0, implies, 0);
					connect(is_man, 0, implies, 1);
					addProcessors(f, is_failure, add, gt, trim, is_man, implies);
					associateInput(f).associateOutput(implies);
				}}));
		
			connect(by_emp, new Println());
			
			/* Push events to illustrate the detection of the violation. The first
			 * two traces for client1 end in a rejection (X), and a "false" verdict
			 * is emitted at the start of the third trace, since the employee
			 * assigned to it is not the manager. */
			Pushable p = by_emp.getPushableInput();
			p.push(new LogUpdate(0, builder.createTuple("client1", "emp1", "a")));
			p.push(new LogUpdate(0, builder.createTuple("client1", "emp1", "X")));
			p.push(new LogUpdate(0, builder.createTuple("client1", "emp1", "END")));
			p.push(new LogUpdate(1, builder.createTuple("client1", "emp2", "a")));
      p.push(new LogUpdate(1, builder.createTuple("client1", "emp2", "X")));
      p.push(new LogUpdate(1, builder.createTuple("client1", "emp2", "END")));
      p.push(new LogUpdate(2, builder.createTuple("client1", "emp3", "a")));
      p.push(new LogUpdate(2, builder.createTuple("client1", "emp3", "X")));
      p.push(new LogUpdate(2, builder.createTuple("client1", "emp3", "END")));
	}

}
