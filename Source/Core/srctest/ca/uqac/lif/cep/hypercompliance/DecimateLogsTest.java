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
 * Unit tests for {@link DecimateLogs}.
 */
public class DecimateLogsTest
{
  @Test
  public void test1()
  {
    DecimateLogs dl = new DecimateLogs(3);
    QueueSink sink = new QueueSink();
    Connector.connect(dl, sink);
    Pushable p = dl.getPushableInput();
    Queue<?> q = sink.getQueue();
    LogUpdate u;
    u = new LogUpdate(0, "a");
    p.push(u);
    assertEquals(u, q.remove());
    u = new LogUpdate(0, "b");
    p.push(u);
    assertEquals(u, q.remove());
    u = new LogUpdate(1, "b");
    p.push(u);
    assertTrue(q.isEmpty());
    u = new LogUpdate(2, "b");
    p.push(u);
    assertTrue(q.isEmpty());
    u = new LogUpdate(0, "c");
    p.push(u);
    assertEquals(u, q.remove());
    u = new LogUpdate(3, "a");
    p.push(u);
    assertEquals(u, q.remove());
  }
}
