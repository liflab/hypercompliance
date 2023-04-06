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
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.tmf.QueueSink;

/**
 * Unit tests for {@link KeepLastEach}.
 */
public class KeepLastEachTest
{
	@Test
	public void test1()
	{
		KeepLastEach k = new KeepLastEach(2);
		QueueSink sink = new QueueSink(2);
		Connector.connect(k, sink);
		Pushable p1 = k.getPushableInput(0);
		Pushable p2 = k.getPushableInput(1);
		Queue<?> q1 = sink.getQueue(0);
		Queue<?> q2 = sink.getQueue(1);
		p1.push(0);
		p2.push(1);
		p1.push(2);
		p1.notifyEndOfTrace();
		assertTrue(q1.isEmpty());
		assertTrue(q2.isEmpty());
		p2.push(3);
		p2.notifyEndOfTrace();
		assertEquals(1, q1.size());
		assertEquals(2, q1.remove());
		assertEquals(1, q2.size());
		assertEquals(3, q2.remove());
	}
}
