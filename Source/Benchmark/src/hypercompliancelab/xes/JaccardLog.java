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
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Sets;
import hypercompliancelab.Describable;

/**
 * Calculates the (generalized) Jaccard index of a log. This is taken as the
 * ratio of the number of events present in <em>all</em> logs to the number of
 * events present in <em>any</em> log.
 *  
 * @author Sylvain Hallé
 */
public class JaccardLog extends GroupProcessor implements Describable
{
	/**
	 * The name of this hyperquery.
	 */
	public static final transient String NAME = "Jaccard log";
	
	public JaccardLog(Function get_action)
	{
		super(1, 1);
		Fork f = new Fork();
		Aggregate agg = new Aggregate(new GroupProcessor(1, 1) {{ 
			ApplyFunction ac = new ApplyFunction(get_action);
			Sets.PutInto all = new Sets.PutInto();
			connect(ac, all);
			addProcessors(ac, all).associateInput(ac).associateOutput(all);
		}},	Choice.INACTIVE, new Sets.Intersect());
		connect(f, 0, agg, 0);
		ApplyFunction all_card = new ApplyFunction(Bags.getSize);
		connect(agg, all_card);
		ApplyFunction ac = new ApplyFunction(new FunctionTree(get_action, new FunctionTree(LogUpdate.getEvent, StreamVariable.X)));
		connect(f, 1, ac, 0);
		Sets.PutInto all = new Sets.PutInto();
		connect(ac, all);
		ApplyFunction some_card = new ApplyFunction(Bags.getSize);
		connect(all, some_card);
		ApplyFunction div = new ApplyFunction(Numbers.division);
		connect(all_card, 0, div, 0);
		connect(some_card, 0, div, 1);
		addProcessors(f, agg, all_card, ac, all, some_card, div).associateInput(f).associateOutput(div);
	}

	@Override
	public String getDescription()
	{
		return "Calculates the (generalized) Jaccard index of a log";
	}
}
