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
import ca.uqac.lif.cep.UniformProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.QueueSink;

/**
 * Unit tests for {@link FilterLogs}.
 */
public class FilterLogsTest
{
  @Test
  public void test1()
  {
    FilterLogs fl = new FilterLogs(new HasA());
    QueueSink sink = new QueueSink();
    Connector.connect(fl, sink);
    Pushable p = fl.getPushableInput();
    Queue<?> q = sink.getQueue();
    p.push(new LogUpdate(0, "b"));
    assertTrue(q.isEmpty());
    p.push(new LogUpdate(1, "a"));
    assertEquals(1, q.size());
    assertEquals(new LogUpdate(1, "a"), q.remove());
    p.push(new LogUpdate(0, "a"));
    assertEquals(2, q.size());
    assertEquals(new LogUpdate(0, "b"), q.remove());
    assertEquals(new LogUpdate(0, "a"), q.remove());
    p.push(new LogUpdate(0, "c"));
    assertEquals(1, q.size());
    assertEquals(new LogUpdate(0, "c"), q.remove());
  }
  
  protected static class HasA extends UniformProcessor
  {
    protected boolean m_seenA = false;
    
    public HasA()
    {
      super(1, 1);
    }

    @Override
    protected boolean compute(Object[] inputs, Object[] outputs)
    {
      m_seenA = m_seenA || inputs[0] == "a";
      outputs[0] = m_seenA ? Troolean.Value.TRUE : Troolean.Value.INCONCLUSIVE;
      return true;
    }

    @Override
    public HasA duplicate(boolean with_state)
    {
      HasA h = new HasA();
      if (with_state)
      {
        h.m_seenA = m_seenA;
      }
      return h;
    }
  }
}
