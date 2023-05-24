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
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.util.Bags.RunOn;
import hypercompliancelab.Describable;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Multiset;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Verifies the hypercompliance policy that at most <i>k</i> process can have
 * the same current activity at any moment.
 * 
 * @author Sylvain Hallé
 */
public class MaxCurrent extends GroupProcessor implements Describable
{
	/**
	 * The name of this hyperquery.
	 */
	public static final transient String NAME = "Max current";
	
	public MaxCurrent(Function get_action, Function get_end, int k)
	{
		super(1, 1);
		Aggregate agg = new Aggregate(new GroupProcessor(1, 1) {{
			DetectEnd end = new DetectEnd(get_end);
			ApplyFunction act = new ApplyFunction(get_action);
			connect(end, act);
			addProcessors(end, act).associateInput(end).associateOutput(act);
		}}, Choice.ACTIVE, new Multiset.PutInto()
		);
		ApplyFunction cards = new ApplyFunction(new FunctionTree(Maps.values, new FunctionTree(Multiset.getCardinalities, StreamVariable.X)));
		connect(agg, cards);
		RunOn max_card = new RunOn(new Cumulate(new CumulativeFunction<Number>(Numbers.maximum)));
		connect(cards, max_card);
		ApplyFunction no_more = new ApplyFunction(new FunctionTree(Numbers.isLessOrEqual, StreamVariable.X, new Constant(k)));
		connect(max_card, no_more);
		addProcessors(agg, cards, max_card, no_more).associateInput(agg).associateOutput(no_more);
	}

	@Override
	public String getDescription()
	{
		return "Verifies the hypercompliance policy that at most <i>k</i> process can have the same current activity at any moment";
	}
}
