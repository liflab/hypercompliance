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

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Numbers;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.Connector;

/**
 * Example of a hyperquery that evaluates the average length (in number
 * of events) of all traces in a log.
 */
public class AverageLength
{
	public static void main(String[] args)
	{
		GroupProcessor length = new GroupProcessor(1, 1);
		{
			TurnInto one = new TurnInto(1);
			Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			connect(one, sum);
			length.addProcessors(one, sum);
			length.associateInput(0, one, 0);
			length.associateOutput(0, sum, 0);
		}
		GroupProcessor avg = new GroupProcessor(1, 1);
		{
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
			avg.addProcessors(f, sum_1, one, sum_2, div);
			avg.associateInput(0, f, 0);
			avg.associateOutput(0, div, 0);
		}
		Aggregate a = new Aggregate(length, Choice.INACTIVE, avg);
		Print print = new Print();
		connect(a, print);
		Pushable p = a.getPushableInput();
		p.push(new LogUpdate(0, "b"));
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(0, null)); // End of trace 0
		p.push(new LogUpdate(1, "b"));
		p.push(new LogUpdate(2, "a"));
		p.push(new LogUpdate(2, null)); // End of trace 2
		p.push(new LogUpdate(1, null)); // End of trace 1
		
	}
}
