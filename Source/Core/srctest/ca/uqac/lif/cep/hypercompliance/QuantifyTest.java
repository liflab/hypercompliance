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
import ca.uqac.lif.cep.functions.UnaryFunction;
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
		Quantify q = new Quantify(new PositivePayload(), IdFunction.instance, QuantifierType.ALL);
		QueueSink sink = new QueueSink();
		Queue<?> queue = sink.getQueue();
		Connector.connect(q, sink);
		Pushable p = q.getPushableInput();
		p.push(new DummyEvent(0, 1));
		assertFalse(queue.isEmpty());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new DummyEvent(1, 1));
		assertFalse(queue.isEmpty());
		assertEquals(Troolean.Value.TRUE, queue.remove());
		p.push(new DummyEvent(0, 0));
		assertFalse(queue.isEmpty());
		assertEquals(Troolean.Value.FALSE, queue.remove());
		
	}
	
	/**
	 * A dummy event made of a trace ID and an arbitrary payload.
	 * Used for testing.
	 */
	public static class DummyEvent
	{
		protected final int m_id;
		
		protected final Object m_payload;
		
		public DummyEvent(int id, Object payload)
		{
			super();
			m_id = id;
			m_payload = payload;
		}
		
		public int getId()
		{
			return m_id;
		}
		
		public Object getPayload()
		{
			return m_payload;
		}
		
		@Override
		public String toString()
		{
			return "(" + m_id + "," + m_payload + ")";
		}
	}
	
	/**
	 * A function extracting the ID from a dummy event.
	 */
	public static class IdFunction extends UnaryFunction<DummyEvent,Integer>
	{
		public static final IdFunction instance = new IdFunction();
		
		protected IdFunction()
		{
			super(DummyEvent.class, Integer.class);
		}

		@Override
		public Integer getValue(DummyEvent x)
		{
			return x.getId();
		}
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
			DummyEvent e = (DummyEvent) inputs[0];
			outputs[0] = Troolean.trooleanValue(((Number) e.getPayload()).intValue() > 0);
			return true;
		}

		@Override
		public Processor duplicate(boolean with_state)
		{
			return new PositivePayload();
		}
		
		
	}
}
