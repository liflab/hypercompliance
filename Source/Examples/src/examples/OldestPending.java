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
import ca.uqac.lif.cep.functions.ContextVariable;
import ca.uqac.lif.cep.functions.RaiseArity;
import ca.uqac.lif.cep.hypercompliance.AggregateSequence;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print.Println;
import ca.uqac.lif.cep.tmf.Prefix;
import ca.uqac.lif.cep.util.Lists;
import examples.MonotonicLength.Counter;

/**
 * At any time point, shows the oldest <i>n</i> pending cases. An "old" case is
 * one that started a long time ago. 
 */
public class OldestPending
{

	public static void main(String[] args)
	{
		/* The number of old cases to show. */
		int n = 3;
		
		AggregateSequence ags = new AggregateSequence(
				new ApplyFunction(new RaiseArity(1, new ContextVariable("id"))),
				Choice.ACTIVE,
				new Counter(),
				new Counter(),
				new GroupProcessor(1, 1) {{
					Prefix p = new Prefix(n);
					Lists.PutInto inlist = new Lists.PutInto();
					connect(p, inlist);
					addProcessors(p, inlist);
					associateInput(p).associateOutput(inlist);
				}}
				);
		connect(ags, new Println());
		Pushable p = ags.getPushableInput();
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(0, "b"));
		p.push(new LogUpdate(1, "a"));
		p.push(new LogUpdate(0, "a"));
	}

}
