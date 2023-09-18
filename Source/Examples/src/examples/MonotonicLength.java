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
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Freeze;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.hypercompliance.AggregateSequence;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;

/**
 * Evaluates the hyperpolicy stipulating that a trace cannot at some point
 * become longer than another trace that started earlier. In other words,
 * one must ensure that a trace does not move forward to "overtake" an older
 * trace.
 * <p>
 * This example makes use of the {@link AggregateSequence} processor, which in
 * this case orders slices according to the moment they start. On each trace,
 * 
 * 
 * @author Sylvain Hallé
 */
public class MonotonicLength
{
	public static void main(String[] args)
	{
		AggregateSequence ags = new AggregateSequence(
				new Counter(), Choice.ALL, new Counter(), new Freeze(),
				new GroupProcessor(1, 1) {{ 
					Fork f = new Fork();
					Trim trim = new Trim(1);
					connect(f, 0, trim, 0);
					ApplyFunction gt = new ApplyFunction(Numbers.isLessOrEqual);
					connect(trim, 0, gt, 0);
					connect(f, 1, gt, 1);
					Cumulate and = new Cumulate(Booleans.and);
					connect(gt, and);
					addProcessors(f, trim, gt, and);
					associateInput(f).associateOutput(and);
				}});
		connect(ags, new Println());
		
		Pushable p = ags.getPushableInput();
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(0, "d"));
		p.push(new LogUpdate(1, "c"));
		p.push(new LogUpdate(2, "c"));
		p.push(new LogUpdate(1, "f"));
	}
	
	/**
	 * A group processor that outputs an incrementing sequence of numbers, and
	 * thus acts as a counter. Since this processor is used in many example
	 * pipelines, it is refactored into this reusable object.
	 */
	protected static class Counter extends GroupProcessor
	{
		public Counter()
		{
			super(1, 1);
			TurnInto one = new TurnInto(1);
			Cumulate sum = new Cumulate(Numbers.addition);
			connect(one, sum);
			addProcessors(one, sum);
			associateInput(one).associateOutput(sum);
		}
	}

}
