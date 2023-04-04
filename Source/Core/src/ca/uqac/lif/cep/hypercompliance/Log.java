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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A modifiable instance of {@link AbstractLog}.
 */
public class Log implements AbstractLog
{  
  /**
   * A map containing the association between trace identifiers and streams.
   */
  /*@ non_null @*/ protected final TreeMap<Object,List<Object>> m_entries;

  /**
   * Creates a new empty log.
   */
  public Log()
  {
    super();
    m_entries = new TreeMap<Object,List<Object>>();
  }
    
  public Log appendTo(Object k, List<Object> trace)
  {
    if (!containsKey(k))
    {
      put(k, trace);
    }
    else
    {
      List<Object> current_trace = get(k);
      current_trace.addAll(trace);
    }
    return this;
  }

  @Override
  /*@ non_null @*/ public Log update(/*@ non_null @*/ Log log)
  {
    for (Map.Entry<Object,List<Object>> e : log.entrySet())
    {
      appendTo(e.getKey(), e.getValue());
    }
    return this;
  }

  @Override
  public boolean containsKey(Object key)
  {
    return m_entries.containsKey(key);
  }

  @Override
  public Set<Entry<Object, List<Object>>> entrySet()
  {
    return m_entries.entrySet();
  }

  @Override
  public boolean equals(Object o)
  {
    return m_entries.equals(o);
  }

  @Override
  public List<Object> get(Object key)
  {
    return m_entries.get(key);
  }

  @Override
  public int hashCode()
  {
    return m_entries.hashCode();
  }

  @Override
  public boolean isEmpty()
  {
    return m_entries.isEmpty();
  }

  @Override
  public Set<Object> keySet()
  {
    return m_entries.keySet();
  }

  @Override
  public int size()
  {
    return m_entries.size();
  }

  public void clear()
  {
    m_entries.clear();
  }

  public List<Object> put(Object key, List<Object> value)
  {
    return m_entries.put(key, value);
  }

  public void putAll(Map<? extends Object, ? extends List<Object>> m)
  {
    m_entries.putAll(m);
  }

  public List<Object> remove(Object key)
  {
    return m_entries.remove(key);
  }

  @Override
  public String toString()
  {
    StringBuilder out = new StringBuilder();
    out.append("{");
    boolean first = true;
    for (Object k : keySet())
    {
      if (first)
      {
        first = false;
      }
      else
      {
        out.append(",");
      }
      out.append(k).append("\u21a6").append(get(k));
    }
    out.append("}");
    return out.toString();
  }
}
