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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.cep.tmf.QueueSource;
import ca.uqac.lif.cep.tuples.Tuple;

public class InterleavedSource extends QueueSource
{
  /**
   * The name of the attribute containing the timestamp of each event.
   */
  /*@ non_null @*/ protected final String m_timestamp;
  
  /**
   * Creates a new interleaved source.
   * @param log The original log containing several non-interleaved instances
   * of a process
   * @param timestamp The name of the attribute containing the timestamp of
   * each event
   */
  public InterleavedSource(/*@ non_null @*/ Log log, /*@ non_null @*/ String timestamp)
  {
    super(1);
    m_timestamp = timestamp;
    List<ComparableLogUpdate> updates = new ArrayList<ComparableLogUpdate>();
    for (Map.Entry<Object,List<Object>> e : log.entrySet())
    {
      Object id = e.getKey();
      List<Object> trace = e.getValue();
      for (Object o : trace)
      {
        ComparableLogUpdate upd = new ComparableLogUpdate(id, (Tuple) o);
        updates.add(upd);
      }
    }
    Collections.sort(updates); // Order by timestamp
    m_events.addAll(updates);
  }
  
  public class ComparableLogUpdate extends LogUpdate implements Comparable<ComparableLogUpdate>
  {
    public ComparableLogUpdate(Object id, Object event)
    {
      super(id, event);
    }

    @Override
    public int compareTo(ComparableLogUpdate o)
    {
      Tuple t1 = (Tuple) getEvent();
      Tuple t2 = (Tuple) o.getEvent();
      long ts1 = ((Number) t1.get(m_timestamp)).longValue();
      long ts2 = ((Number) t2.get(m_timestamp)).longValue();
      if (ts1 < ts2)
      {
        return -1;
      }
      if (ts1 > ts1)
      {
        return 1;
      }
      return 0;
    }
  }
}
