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

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import ca.uqac.lif.cep.SynchronousProcessor;

public class DecimateLogs extends SynchronousProcessor
{
  /**
   * The decimation interval.
   */
  /*@ non_null @*/ protected final int m_interval;
  
  /**
   * A set keeping track of all the stream identifiers received since the
   * start of the processor.
   */
  /*@ non_null @*/ protected final Set<Object> m_seenIds;
  
  /**
   * A set keeping track of those stream identifiers that correspond to
   * those whose updates must be kept.
   */
  /*@ non_null @*/ protected final Set<Object> m_keptIds;
  
  /**
   * Creates a new instance of the processor.
   * @param interval The decimation interval
   */
  public DecimateLogs(int interval)
  {
    super(1, 1);
    m_interval = interval;
    m_seenIds = new HashSet<Object>();
    m_keptIds = new HashSet<Object>();
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    LogUpdate upd = (LogUpdate) inputs[0];
    Object id = upd.getId();
    if (!m_seenIds.contains(id))
    {
      if (m_seenIds.size() % m_interval == 0)
      {
        m_keptIds.add(id);
      }
      m_seenIds.add(id);
    }
    if (m_keptIds.contains(id))
    {
      outputs.add(new Object[] {upd});
    }
    return true;
  }

  @Override
  public DecimateLogs duplicate(boolean with_state)
  {
    DecimateLogs d = new DecimateLogs(m_interval);
    if (with_state)
    {
      d.m_keptIds.addAll(m_keptIds);
      d.m_seenIds.addAll(m_seenIds);
    }
    return d;
  }
}
