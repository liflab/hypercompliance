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
package hypercompliancelab.xes;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.hypercompliance.SliceLogLast;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.tuples.MergeScalars;
import ca.uqac.lif.cep.util.Multiset;
import hypercompliancelab.Describable;

/**
 * Calculates the set of pairs of successive events in each log, and keeps
 * the count of how many times they each occur.
 * 
 * @author Sylvain Hallé
 */
public class DirectlyFollows extends GroupProcessor implements Describable
{
	/**
	 * The name of this hyperquery.
	 */
	public static final transient String NAME = "Directly follows";
	
	public DirectlyFollows(Function get_action)
	{
		super(1, 1);
		SliceLogLast slice1 = new SliceLogLast(new GroupProcessor(1, 1) {{
			ApplyFunction get_ev = new ApplyFunction(get_action);
			Fork f = new Fork();
			connect(get_ev, f);
			ApplyFunction to_array = new ApplyFunction(new MergeScalars("from", "to"));
			connect(f, 0, to_array, 0);
			Trim t = new Trim(1);
			connect(f, 1, t, 0);
			connect(t, 0, to_array, 1);
			addProcessors(get_ev, f, t, to_array).associateInput(get_ev).associateOutput(to_array);
		}}, Choice.ALL);
		Multiset.PutInto pi = new Multiset.PutInto();
		connect(slice1, pi);
		addProcessors(slice1, pi).associateInput(slice1).associateOutput(pi);
	}

	@Override
	public String getDescription()
	{
		return "Calculates the set of pairs of successive events in each log, and keeps the count of how many times they each occur";
	}
}
