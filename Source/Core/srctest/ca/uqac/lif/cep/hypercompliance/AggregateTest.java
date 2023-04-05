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
package ca.uqac.lif.cep.hypercompliance;

import static org.junit.Assert.*;

import java.util.Queue;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Unit tests for {@link SliceLog}.
 */
public class AggregateTest
{
	@Test
	public void test1()
	{
		Aggregate sl = new Aggregate(new Passthrough(), new Cumulate(new CumulativeFunction<Number>(Numbers.addition)));
		QueueSink sink = new QueueSink();
		Connector.connect(sl, sink);
		Pushable p = sl.getPushableInput();
		Queue<?> q = sink.getQueue();
		float out;
		// Event
		p.push(new LogUpdate(0, 1));
		assertEquals(1, q.size());
		out = (Float) q.remove();
		assertEquals(1f, out, 0f);
		// Event
		p.push(new LogUpdate(1, 2));
		assertEquals(1, q.size());
		out = (Float) q.remove();
		assertEquals(3f, out, 0f);
		// Event
		p.push(new LogUpdate(1, 10));
		assertEquals(1, q.size());
		out = (Float) q.remove();
		assertEquals(11f, out, 0f);
	}
	
	@Test
	public void test2()
	{
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
		Aggregate sl = new Aggregate(new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), avg);
		QueueSink sink = new QueueSink();
		Connector.connect(sl, sink);
		Pushable p = sl.getPushableInput();
		Queue<?> q = sink.getQueue();
		float out;
		// Event
		p.push(new LogUpdate(0, 1));
		assertEquals(1, q.size());
		out = (Float) q.remove();
		assertEquals(1f, out, 0f);
		// Event
		p.push(new LogUpdate(1, 2));
		assertEquals(1, q.size());
		out = (Float) q.remove();
		assertEquals(1.5f, out, 0f);
		// Event
		p.push(new LogUpdate(1, 9));
		assertEquals(1, q.size());
		out = (Float) q.remove();
		assertEquals(6f, out, 0f);
	}
}
