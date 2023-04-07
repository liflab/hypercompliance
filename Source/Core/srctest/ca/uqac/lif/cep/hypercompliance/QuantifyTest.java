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
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.hypercompliance.Quantify.QuantifierType;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.QueueSink;

/**
 * Unit tests for {@link Quantify}.
 */
public class QuantifyTest
{
	@Test
	public void test1()
	{
		Quantify q = new Quantify(new PositivePayload(), false, QuantifierType.ALL);
		QueueSink sink = new QueueSink();
		Queue<?> queue = sink.getQueue();
		Connector.connect(q, sink);
		Pushable p = q.getPushableInput();
		p.push(new LogUpdate(0, 1));
		assertFalse(queue.isEmpty());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new LogUpdate(1, 1));
		assertFalse(queue.isEmpty());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new LogUpdate(0, 0));
		assertFalse(queue.isEmpty());
		assertEquals(Troolean.Value.FALSE, queue.remove());
		
	}
	
	@Test
	public void test2()
	{
		Quantify q = new Quantify(new SameModulo(), false, QuantifierType.ALL, QuantifierType.ALL);
		QueueSink sink = new QueueSink();
		Queue<?> queue = sink.getQueue();
		Connector.connect(q, sink);
		Pushable p = q.getPushableInput();
		p.push(new LogUpdate(0, 1));
		assertEquals(1, queue.size());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new LogUpdate(1, 11));
		assertEquals(1, queue.size());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new LogUpdate(1, 12));
		assertEquals(1, queue.size());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new LogUpdate(0, 3));
		assertEquals(1, queue.size());
		assertEquals(Troolean.Value.FALSE, queue.remove());
	}
	
	public static class PositivePayload extends UniformProcessor
	{
		public PositivePayload()
		{
			super(1, 1);
		}

		@Override
		protected boolean compute(Object[] inputs, Object[] outputs)
		{
			Integer i = (Integer) inputs[0];
			outputs[0] = Troolean.trooleanValue(i > 0);
			return true;
		}

		@Override
		public Processor duplicate(boolean with_state)
		{
			return new PositivePayload();
		}
	}
	
	public static class SameModulo extends UniformProcessor
	{
		public SameModulo()
		{
			super(2, 1);
		}

		@Override
		protected boolean compute(Object[] inputs, Object[] outputs)
		{
			Integer i1 = (Integer) inputs[0];
			Integer i2 = (Integer) inputs[1];
			int mod1 = i1 % 2;
			int mod2 = i2 % 2;
			System.out.println("Comparing " + i1 + " vs " + i2);
			outputs[0] = Troolean.trooleanValue(mod1 == mod2);
			return true;
		}

		@Override
		public Processor duplicate(boolean with_state)
		{
			return new SameModulo();
		}
	}
}
