/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2023 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.hypercompliance;

import static org.junit.Assert.assertEquals;

import java.util.Queue;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.QueueSink;

/**
 * Unit tests for {@link MonitorFilter}.
 */
public class MonitorFilterTest
{
	@Test
	public void test1()
	{
		MonitorFilter f = new MonitorFilter(new EventuallyA());
		QueueSink sink = new QueueSink();
		Connector.connect(f, sink);
		Pushable p = f.getPushableInput();
		Queue<?> q = sink.getQueue();
		p.push("b");
		assertEquals(0, q.size());
		p.push("c");
		assertEquals(0, q.size());
		p.push("a");
		assertEquals(3, q.size());
		assertEquals("b", q.remove());
		assertEquals("c", q.remove());
		assertEquals("a", q.remove());
		p.push("d");
		assertEquals(1, q.size());
		assertEquals("d", q.remove());
		
	}
	
	@Test
	public void test2()
	{
		MonitorFilter f = new MonitorFilter(new EventuallyA());
		QueueSink sink = new QueueSink();
		Connector.connect(f, sink);
		Pushable p = f.getPushableInput();
		Queue<?> q = sink.getQueue();
		p.push("a");
		assertEquals(1, q.size());
	}

	protected static class EventuallyA extends UniformProcessor
	{
		protected boolean m_seenA;

		public EventuallyA()
		{
			super(1, 1);
			m_seenA = false;
		}

		@Override
		protected boolean compute(Object[] inputs, Object[] outputs)
		{
			if (inputs[0] == "a")
			{
				m_seenA = true;
			}
			if (m_seenA)
			{
				outputs[0] = Troolean.Value.TRUE;
				return true;
			}
			outputs[0] = Troolean.Value.INCONCLUSIVE;
			return true;
		}
		
		@Override
		public void reset()
		{
			super.reset();
			m_seenA = false;
		}

		@Override
		public Processor duplicate(boolean with_state)
		{
			// TODO Auto-generated method stub
			return null;
		}
	}
}
