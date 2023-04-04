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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * A processor that releases log update events from traces in a log only for
 * traces that satisfy a given Troolean property.
 * 
 * @author Sylvain Hallé
 *
 */
public class FilterLogs extends SynchronousProcessor
{
  protected final Processor m_condition;

  protected final Map<Object,PushUnit> m_units;

  public FilterLogs(Processor condition)
  {
    super(1, 1);
    m_condition = condition;
    m_units = new TreeMap<Object,PushUnit>();
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    LogUpdate upd = (LogUpdate) inputs[0];
    Object id = upd.getId();
    if (!m_units.containsKey(id))
    {
      m_units.put(id, new PushUnit());
    }
    PushUnit pu = m_units.get(id);
    List<Object> out = pu.push(upd);
    if (out != null)
    {
      for (Object e : out)
      {
        outputs.add(new Object[] {e});
      }
    }
    return true;
  }

  @Override
  public FilterLogs duplicate(boolean with_state)
  {
    throw new UnsupportedOperationException("This processor cannot be duplicated");
  }

  protected class PushUnit
  {
    protected final Processor m_processor;

    protected final Pushable m_pushable;

    protected final SinkLast m_sink;

    protected final List<Object> m_retained;

    protected Troolean.Value m_verdict;

    public PushUnit()
    {
      super();
      m_processor = m_condition.duplicate();
      m_pushable = m_processor.getPushableInput();
      m_sink = new SinkLast();
      Connector.connect(m_processor, m_sink);
      m_retained = new ArrayList<Object>();
      m_verdict = Troolean.Value.INCONCLUSIVE;
    }

    /*@ null @*/ public List<Object> push(LogUpdate u)
    {
      if (m_verdict == Troolean.Value.FALSE)
      {
        // Don't bother
        return null;
      }
      m_pushable.push(u.getEvent());
      List<Object> to_output = new ArrayList<Object>();
      if (m_verdict == Troolean.Value.INCONCLUSIVE)
      {
        m_verdict = (Troolean.Value) m_sink.getLast()[0];
        if (m_verdict == Troolean.Value.FALSE)
        {
          m_retained.clear();
          return null;
        }
        else if (m_verdict == Troolean.Value.TRUE)
        {
          to_output.addAll(m_retained);
          m_retained.clear(); 
        }
        else
        {
          m_retained.add(u);
        }
      }
      if (m_verdict == Troolean.Value.TRUE)
      {
        to_output.add(u);
      }
      return to_output;
    }
  }
}
