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
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Trim;
import ca.uqac.lif.cep.util.Numbers;
import hypercompliancelab.Describable;

/**
 * Calculates the mean time interval between two successive log update events.
 * @author Sylvain Hallé
 */
public class MeanInterval extends GroupProcessor implements Describable
{
	/**
	 * The name of this hyperquery.
	 */
	public static final transient String NAME = "Mean time interval";
	
	public MeanInterval(Function get_timestamp)
	{
		super(1, 1);
		ApplyFunction ts = new ApplyFunction(new FunctionTree(Numbers.division, new FunctionTree(get_timestamp, new FunctionTree(LogUpdate.getEvent, StreamVariable.X)), new Constant(1000)));
		Fork f1 = new Fork();
		connect(ts, f1);
		Trim tr = new Trim(1);
		connect(f1, 0, tr, 0);
		ApplyFunction minus = new ApplyFunction(Numbers.subtraction);
		connect(tr, 0, minus, 0);
		connect(f1, 1, minus, 1);
		Fork f2 = new Fork();
		connect(minus, f2);
		Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
		connect(f2, 0, sum, 0);
		TurnInto one = new TurnInto(1);
		connect(f2, 1, one, 0);
		Cumulate sum_one = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
		connect(one, 0, sum_one, 0);
		ApplyFunction div = new ApplyFunction(Numbers.division);
		connect(sum, 0, div, 0);
		connect(sum_one, 0, div, 1);
		addProcessors(ts, f1, tr, minus, f2, sum, one, sum_one, div).associateInput(ts).associateOutput(div);
	}

	@Override
	public String getDescription()
	{
		return "Calculates the mean time interval between two successive log update events";
	}
}
