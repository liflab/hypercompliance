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
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tmf.Window;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;
import examples.MonotonicLength.Counter;

/**
 * Generates a warning whenever the duration of <i>n</i> successive traces
 * is increasing.
 * <p>
 * For the sake of this example, "duration" is taken as the number of events in
 * the trace. It could be replaced by, e.g. the difference between the last
 * and the first timestamp.
 */
public class IncreasingDuration
{
	public static void main(String[] args)
	{
		/* The number of successive traces that must have an increasing length
		 * in order to trigger the warning. */
		int n = 3;
		
		AggregateSequence ags = new AggregateSequence(
				new GroupProcessor(1, 1) {{
					DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant("END")));
					Counter cnt = new Counter();
					connect(end, cnt);
					addProcessors(end, cnt);
					associateInput(end).associateOutput(cnt);
				}},
				Choice.INACTIVE,
				new Counter(),
				new KeepLast(),
				new Window(new GroupProcessor(1, 1) {{
					Fork f = new Fork();
					Trim trim = new Trim(1);
					connect(f, 0, trim, 0);
					ApplyFunction gt = new ApplyFunction(Numbers.isGreaterThan);
					connect(trim, 0, gt, 0);
					connect(f, 1, gt, 1);
					Cumulate all = new Cumulate(Booleans.and);
					connect(gt, all);
					addProcessors(f, trim, gt, f, all);
					associateInput(f).associateOutput(all);
				}}, n));
		connect(ags, new Println());
		Pushable p = ags.getPushableInput();
		
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(0, "END"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(1, "END"));
		p.push(new LogUpdate(2, "a"));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(2, "END"));
		p.push(new LogUpdate(3, "a"));
		p.push(new LogUpdate(3, "b"));
		p.push(new LogUpdate(3, "c"));
		p.push(new LogUpdate(3, "END"));
		p.push(new LogUpdate(4, "a"));
		p.push(new LogUpdate(4, "b"));
		p.push(new LogUpdate(4, "c"));
		p.push(new LogUpdate(4, "d"));
		p.push(new LogUpdate(4, "END"));
		
	}
}
