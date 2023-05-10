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

import ca.uqac.lif.cep.tuples.Tuple;

/**
 * A source of {@link LogUpdate} events obtained from a {@link Log} by
 * outputting the events in the order defined by their timestamp.
 * 
 * @author Sylvain Hallé
 */
public class InterleavedSource extends LogSource
{
  /**
   * The name of the attribute containing the timestamp of each event.
   */
  /*@ non_null @*/ protected final String m_timestamp;
  
  public InterleavedSource(String timestamp)
  {
    super();
    m_timestamp = timestamp;
  }
  
  /**
   * Creates a new interleaved source.
   * @param log The original log containing several non-interleaved instances
   * of a process
   * @param timestamp The name of the attribute containing the timestamp of
   * each event
   */
  public InterleavedSource(/*@ non_null @*/ Log log, /*@ non_null @*/ String timestamp)
  {
    super();
    m_timestamp = timestamp;
    populateFromLog(log);
  }
  
  /**
   * Populates the internal queue of events by interleaving the events from the
   * log according to their timestamp.
   * @param log The log to read the events from
   */
  protected void populateFromLog(Log log)
  {
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
  
  /**
   * A {@link LogUpdate} that implements the {@link Comparable} interface by
   * referring to the timestamp attribute of the event they contain. This
   * allows these updates to be put in a list and then sorted according to
   * the timestamp of the contained event.
   */
  public class ComparableLogUpdate extends LogUpdate implements Comparable<ComparableLogUpdate>
  {
    /**
     * Produces a comparable log update for a trace ID.
     * @param id The trace ID
     * @param event The tuple event to append to that trace
     */
    public ComparableLogUpdate(Object id, Tuple event)
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
