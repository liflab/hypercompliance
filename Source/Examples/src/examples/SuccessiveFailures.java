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
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Window;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;
import examples.MonotonicLength.Counter;

/**
 * Evaluates the hyperpolicy stipulating that at most <i>m</i> out of <i>n</i>
 * successive traces may end in a failure. In this simple example, "failure" is
 * symbolized by the fact that the last event of the trace is a specific symbol
 * (here the string <tt>"X"</tt>).
 * <p>
 * For example, the table below shows a sequence of 10 interleaved events from
 * four different traces (with slice IDs ranging from 0 to 3); the bullet
 * symbol "&bull;" indicates that the event marks the end of the slice. This
 * sequence violates the property with <i>m</i>=2 and <i>n</i>=2, at the 10th
 * event, since at this point, 2 out of the last 2 slices that ended finished
 * in a failure.
 * <p> 
 * <table border="1">
 * <tr><th>ID</th><th>1</th><th>2</th><th>3</th><th>4</th><th>5</th><th>6</th><th>7</th><th>8</th><th>9</th><th>10</th></tr>
 * <tr><th>0</th> <td>a</td><td> </td><td> </td><td> </td><td>b</td><td> </td><td> </td><td> </td><td> </td><td>X&bull;</td></tr>
 * <tr><th>1</th> <td> </td><td>a</td><td>X</td><td> </td><td> </td><td> </td><td>c&bull;</td><td></td><td> </td><td> </td></tr>
 * <tr><th>2</th> <td> </td><td> </td><td> </td><td>a</td><td> </td><td>b</td><td> </td><td>X&bull;</td><td> </td><td> </td></tr>
 * <tr><th>3</th> <td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>a</td><td> </td></tr>
 * </table>
 * <p>
 * On the contrary, the following sequence does <em>not</em> violate the
 * condition:
 * <p> 
 * <table border="1">
 * <tr><th>ID</th><th>1</th><th>2</th><th>3</th><th>4</th><th>5</th><th>6</th><th>7</th><th>8</th><th>9</th><th>10</th></tr>
 * <tr><th>0</th> <td>a</td><td> </td><td> </td><td> </td><td>b</td><td> </td><td> </td><td> </td><td> </td><td>X&bull;</td></tr>
 * <tr><th>1</th> <td> </td><td>a</td><td>X</td><td> </td><td> </td><td> </td><td></td><td>c&bull;</td><td> </td><td> </td></tr>
 * <tr><th>2</th> <td> </td><td> </td><td> </td><td>a</td><td> </td><td>b</td><td>X&bull;</td><td></td><td> </td><td> </td></tr>
 * <tr><th>3</th> <td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td> </td><td>a</td><td> </td></tr>
 * </table>
 * <p>
 * This example makes use of the {@link AggregateSequence} processor, which in
 * this case orders slices according to the moment they end.
 *  
 * @author Sylvain Hallé
 */
public class SuccessiveFailures
{
	public static void main(String[] args)
	{
		int m = 2, n = 2;
		
		AggregateSequence ags = new AggregateSequence(
			new GroupProcessor(1, 1) {{
				DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant("END")));
				KeepLast last = new KeepLast();
				connect(end, last);
				ApplyFunction is_failure = new ApplyFunction(new FunctionTree(Equals.instance, StreamVariable.X, new Constant("X")));
				connect(last, is_failure);
				addProcessors(end, last, is_failure);
				associateInput(end).associateOutput(is_failure);
			}},
			Choice.INACTIVE,
			new Counter(),
			new KeepLast(),
			new GroupProcessor(1, 1) {{
				Window win = new Window(new GroupProcessor(1, 1) {{
					ApplyFunction is_true = new ApplyFunction(new FunctionTree(IfThenElse.instance, StreamVariable.X, new Constant(1), new Constant(0)));
					Cumulate sum = new Cumulate(Numbers.addition);
					connect(is_true, sum);
					addProcessors(is_true, sum);
					associateInput(is_true).associateOutput(sum);
					}}, n);
				ApplyFunction gt = new ApplyFunction(new FunctionTree(Numbers.isLessThan, StreamVariable.X, new Constant(m)));
				connect(win, gt);
				addProcessors(win, gt);
				associateInput(win).associateOutput(gt);
			}}
			);
		
		connect(ags, new Println());
		Pushable p = ags.getPushableInput();
		
		/* Replaying the trace corresponding to the first table in the Javadoc
		 * above. The pipeline should return a false verdict at the last event. */
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(1, "X"));
		p.push(new LogUpdate(2, "a"));
		p.push(new LogUpdate(0, "b"));
		p.push(new LogUpdate(2, "b"));
		p.push(new LogUpdate(1, "c"));
		p.push(new LogUpdate(1, "END"));
		p.push(new LogUpdate(2, "X"));
		p.push(new LogUpdate(2, "END"));
		p.push(new LogUpdate(3, "a"));
		p.push(new LogUpdate(0, "X"));
		p.push(new LogUpdate(0, "END"));
	}

}
