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

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.PushUnit;

/**
 * A processor that releases log update events from traces in a log only for
 * traces that satisfy a given Troolean property.
 * <p>
 * The processor evaluating the condition can perform an arbitrary calculation.
 * The only condition is that it returns a stream of values of type
 * {@link Troolean.Value}, and that its output be <em>monotonic</em>: if it
 * returns {@code TRUE} at some point, it must then return {@code TRUE} forever
 * (and the same for {@code FALSE}).
 * 
 * @author Sylvain Hallé
 */
public class FilterLogs extends SynchronousProcessor
{
  /**
   * The processor evaluating the condition on each trace.
   */
  /*@ non_null @*/ protected final Processor m_condition;

  /**
   * A map associating trace IDs to their corresponding {@link FilterPushUnit}.
   */
  /*@ non_null @*/ protected final Map<Object,FilterPushUnit> m_units;

  /**
   * Creates a new instance of the processor.
   * @param condition The processor evaluating the condition on each trace
   */
  public FilterLogs(/*@ non_null @*/ Processor condition)
  {
    super(1, 1);
    m_condition = condition;
    m_units = new TreeMap<Object,FilterPushUnit>();
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    LogUpdate upd = (LogUpdate) inputs[0];
    Object id = upd.getId();
    if (!m_units.containsKey(id))
    {
      m_units.put(id, new FilterPushUnit(m_condition.duplicate()));
    }
    FilterPushUnit pu = m_units.get(id);
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

  /**
   * The encapsulation of a processor connected to a sink. In addition, the
   * class controls what events pushed to the processor are allowed to be
   * released, by looking at the Troolean verdict produced by the processor
   * (see {@link #push(LogUpdate)}).
   */
  protected class FilterPushUnit extends PushUnit
  {

    /**
     * The list of events pushed to this processor and that have not been
     * released yet.
     */
    /*@ non_null @*/ protected final List<Object> m_retained;

    /**
     * A flag indicating if the events pushed to this processor are allowed to
     * be let through.
     */
    /*@ non_null @*/ protected Troolean.Value m_verdict;

    /**
     * Creates a new push unit.
     */
    public FilterPushUnit(Processor p)
    {
      super(p);
      m_retained = new ArrayList<Object>();
      m_verdict = Troolean.Value.INCONCLUSIVE;
    }

    /**
     * Pushes a {@link LogUpdate} event to this push unit. This pushes the
     * event to the underlying processor, after which the verdict produced by
     * the processor is queried.
     * <ul>
     * <li>If the verdict is {@code INCONCLUSIVE}, the event is added to the
     * "retained" list and nothing else happens.</li>
     * <li>If the verdict is {@code TRUE}, the list of retained events is
     * let through, followed by the newly pushed event. Then the list of
     * retained events is cleared.</li>
     * <li>If the verdict is {@code FALSE}, no event is output.</li>
     * </ul>
     * @param u The log update event to push
     * @return A list of events that are allowed to be let through by the
     * encasing {@link FilterLogs} processor.
     */
    /*@ null @*/ public List<Object> push(LogUpdate u)
    {
      if (m_verdict == Troolean.Value.FALSE)
      {
        // Don't bother
        return null;
      }
      super.push(u);
      List<Object> to_output = new ArrayList<Object>();
      if (m_verdict == Troolean.Value.INCONCLUSIVE)
      {
        m_verdict = (Troolean.Value) getLast();
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
