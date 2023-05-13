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

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Numbers;
import hypercompliancelab.Describable;

/**
 * Evaluates the average length of a trace in a log.
 * @author Sylvain Hallé
 */
public class AverageLength extends Aggregate implements Describable
{
	/**
	 * The name of this hyperquery.
	 */
	public static final transient String NAME = "Average length";
	
	public AverageLength(Function end_condition)
	{
		super(getPerSlice(end_condition), Choice.INACTIVE, getAggregation());
	}
	
	protected static final GroupProcessor getPerSlice(Function end_condition)
	{
		return new GroupProcessor(1, 1) {{
			DetectEnd end = new DetectEnd(end_condition);
			TurnInto one = new TurnInto(1);
			connect(end, one);
			Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			connect(one, sum);
			addProcessors(end, one, sum);
			associateInput(0, end, 0);
			associateOutput(0, sum, 0);
		}};
	}
	
	protected static final GroupProcessor getAggregation()
	{
		return new GroupProcessor(1, 1) {{
			Fork f = new Fork();
			Cumulate sum_1 = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			Connector.connect(f, 0, sum_1, 0);
			TurnInto one = new TurnInto(1);
			Connector.connect(f, 1, one, 0);
			Cumulate sum_2 = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			Connector.connect(one, sum_2);
			ApplyFunction div = new ApplyFunction(Numbers.division);
			Connector.connect(sum_1, 0, div, 0);
			Connector.connect(sum_2, 0, div, 1);
			addProcessors(f, sum_1, one, sum_2, div);
			associateInput(0, f, 0);
			associateOutput(0, div, 0);
		}};
	}

	@Override
	public String getDescription()
	{
		return "Evaluates the average length of a trace in a log";
	}
}
