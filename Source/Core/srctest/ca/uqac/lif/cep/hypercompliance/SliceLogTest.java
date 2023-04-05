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

import java.util.Map;
import java.util.Queue;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Unit tests for {@link SliceLog}.
 */
public class SliceLogTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void test1()
	{
		SliceLog sl = new SliceLog(new Cumulate(new CumulativeFunction<Number>(Numbers.addition)));
		QueueSink sink = new QueueSink();
		Connector.connect(sl, sink);
		Pushable p = sl.getPushableInput();
		Queue<?> q = sink.getQueue();
		Map<Object,Object> map;
		// Event
		p.push(new LogUpdate(0, 1));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(1, map.size());
		assertEquals(1f, map.get(0));
		// Event
		p.push(new LogUpdate(0, 2));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(1, map.size());
		assertEquals(3f, map.get(0));
		// Event
		p.push(new LogUpdate(1, 10));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(2, map.size());
		assertEquals(3f, map.get(0));
		assertEquals(10f, map.get(1));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test2()
	{
		SliceLog sl = new SliceLog(new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), Choice.INACTIVE);
		QueueSink sink = new QueueSink();
		Connector.connect(sl, sink);
		Pushable p = sl.getPushableInput();
		Queue<?> q = sink.getQueue();
		Map<Object,Object> map;
		// Event
		p.push(new LogUpdate(0, 1));
		map = (Map<Object,Object>) q.remove();
		assertTrue(map.isEmpty());
		// Event
		p.push(new LogUpdate(0, 2));
		map = (Map<Object,Object>) q.remove();
		assertTrue(map.isEmpty());
		// Event
		p.push(new LogUpdate(1, 10));
		map = (Map<Object,Object>) q.remove();
		assertTrue(map.isEmpty());
		// No more events for slice 0
		p.push(new LogUpdate(0, null));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(1, map.size());
		assertEquals(3f, map.get(0));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test3()
	{
		SliceLog sl = new SliceLog(new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), Choice.ACTIVE);
		QueueSink sink = new QueueSink();
		Connector.connect(sl, sink);
		Pushable p = sl.getPushableInput();
		Queue<?> q = sink.getQueue();
		Map<Object,Object> map;
		// Event
		p.push(new LogUpdate(0, 1));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(1, map.size());
		assertEquals(1f, map.get(0));
		// Event
		p.push(new LogUpdate(0, 2));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(1, map.size());
		assertEquals(3f, map.get(0));
		// Event
		p.push(new LogUpdate(1, 10));
		map = (Map<Object,Object>) q.remove();
		assertEquals(2, map.size());
		assertEquals(3f, map.get(0));
		assertEquals(10f, map.get(1));
		// No more events for slice 0
		p.push(new LogUpdate(0, null));
		assertEquals(1, q.size());
		map = (Map<Object,Object>) q.remove();
		assertEquals(1, map.size());
		assertEquals(10f, map.get(1));
	}
}
