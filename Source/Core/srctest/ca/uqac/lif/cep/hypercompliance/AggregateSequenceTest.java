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

import java.util.List;
import java.util.Queue;

import org.junit.Test;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.tmf.Freeze;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.QueueSink;
import ca.uqac.lif.cep.util.Lists;

/**
 * Unit tests for {@link AggregateSequence}.
 */
public class AggregateSequenceTest
{
	@Test
	public void test1()
	{
		AggregateSequence ags = new AggregateSequence(new Passthrough(), Choice.ALL, new Passthrough(), new Freeze(), new Lists.PutInto());
		QueueSink sink = new QueueSink();
		Connector.connect(ags, sink);
		Pushable p = ags.getPushableInput();
		Queue<?> q = sink.getQueue();
		List<?> list;
		p.push(new LogUpdate(0, "a"));
		list = (List<?>) q.remove();
		assertEquals("a", list.get(0));
		p.push(new LogUpdate(1, "b"));
		list = (List<?>) q.remove();
		assertEquals("a", list.get(0));
		assertEquals("b", list.get(1));
		p.push(new LogUpdate(1, "c"));
		list = (List<?>) q.remove();
		assertEquals("a", list.get(0));
		assertEquals("c", list.get(1));
		p.push(new LogUpdate(0, "d"));
		list = (List<?>) q.remove();
		assertEquals("d", list.get(0));
		assertEquals("c", list.get(1));
	}
	
	@Test
	public void test2()
	{
		AggregateSequence ags = new AggregateSequence(new Passthrough(), Choice.ALL, new Passthrough(), new Freeze(), new Lists.PutInto());
		QueueSink sink = new QueueSink();
		Connector.connect(ags, sink);
		Pushable p = ags.getPushableInput();
		Queue<?> q = sink.getQueue();
		List<?> list;
		p.push(new LogUpdate(0, "b"));
		list = (List<?>) q.remove();
		assertEquals("b", list.get(0));
		p.push(new LogUpdate(1, "a"));
		list = (List<?>) q.remove();
		assertEquals("a", list.get(0));
		assertEquals("b", list.get(1));
		p.push(new LogUpdate(1, "c"));
		list = (List<?>) q.remove();
		assertEquals("c", list.get(0));
		assertEquals("b", list.get(1));
		p.push(new LogUpdate(0, "d"));
		list = (List<?>) q.remove();
		assertEquals("c", list.get(0));
		assertEquals("d", list.get(1));
	}
}
