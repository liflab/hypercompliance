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
import ca.uqac.lif.cep.functions.IfThenElse;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.Connector;

/**
 * Example of a hyperquery that evaluates the fraction of all traces
 * in a log that end with an event "a".
 */
public class FractionEndInA
{
	public static void main(String[] args)
	{
		GroupProcessor current_a = new GroupProcessor(1, 1);
		{
			Fork f = new Fork(3);
			Fork f2 = new Fork();
			connect(f, 0, f2, 0);
			ApplyFunction eq = new ApplyFunction(Equals.instance);
			connect(f2, 0, eq, 0);
			TurnInto a = new TurnInto("a");
			connect(f2, 1, a, 0);
			connect(a, 0, eq, 1);
			ApplyFunction ite = new ApplyFunction(IfThenElse.instance);
			connect(eq, 0, ite, 0);
			TurnInto one = new TurnInto(1);
			connect(f, 1, one, 0);
			TurnInto zero = new TurnInto(0);
			connect(f, 2, zero, 0);
			connect(one, 0, ite, 1);
			connect(zero, 0, ite, 2);
			current_a.addProcessors(f, f2, eq, a, one, zero, ite);
			current_a.associateInput(0, f, 0);
			current_a.associateOutput(0, ite, 0);
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
		Aggregate a = new Aggregate(current_a, Choice.INACTIVE, avg);
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
