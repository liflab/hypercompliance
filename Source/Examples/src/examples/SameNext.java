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
import ca.uqac.lif.cep.functions.IdentityFunction;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLogLast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Freeze;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Booleans;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.NthElement;

/**
 * Determines what actions in a log are always followed by the same
 * subsequent action in every trace.
 * 
 * @author Sylvain Hallé
 */
public class SameNext
{
	public static void main(String[] args)
	{
		SliceLogLast slice1 = new SliceLogLast(new GroupProcessor(1, 1) {{
			ApplyFunction get_ev = new ApplyFunction(new IdentityFunction(1));
			Fork f = new Fork();
			connect(get_ev, f);
			ApplyFunction to_array = new ApplyFunction(new Bags.ToArray(2));
			connect(f, 0, to_array, 0);
			Trim t = new Trim(1);
			connect(f, 1, t, 0);
			connect(t, 0, to_array, 1);
			addProcessors(get_ev, f, t, to_array).associateInput(get_ev).associateOutput(to_array);
		}});
		
		Slice slice2 = new Slice(new NthElement(0), new GroupProcessor(1, 1) {{
			ApplyFunction get_second = new ApplyFunction(new NthElement(1));
			Fork f = new Fork();
			connect(get_second, f);
			Freeze fr = new Freeze();
			connect(f, 0, fr, 0);
			ApplyFunction eq = new ApplyFunction(Equals.instance);
			connect(fr, 0, eq, 0);
			connect(f, 1, eq, 1);
			Cumulate and = new Cumulate(new CumulativeFunction<Boolean>(Booleans.and));
			connect(eq, and);
			addProcessors(get_second, f, fr, eq, and).associateInput(get_second).associateOutput(and);
		}});
		
		connect(slice1, slice2);
		
		Pushable p = slice1.getPushableInput();
		QueueSink sink = new QueueSink();
		connect(slice2, sink);
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(0, "b"));
		Object o = sink.getQueue().remove();
		System.out.println(o);
		p.push(new LogUpdate(1, "c"));
		o = sink.getQueue().remove();
		System.out.println(o);
		p.push(new LogUpdate(1, "d"));
		o = sink.getQueue().remove();
		System.out.println(o);
	}
}
