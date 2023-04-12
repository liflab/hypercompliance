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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import ca.uqac.lif.cep.functions.UnaryFunction;

import java.util.Set;

/**
 * A log that contains a single trace made of a single event. These logs are
 * meant as "updates" to an existing log, and are read-only objects.
 * @author Sylvain Hallé
 */
public class LogUpdate implements AbstractLog
{
	/**
	 * A single publicly visible instance of the {@link GetEvent} function.
	 */
	/*@ non_null @*/ public static final GetEvent getEvent = new GetEvent();
	
	/**
	 * A single publicly visible instance of the {@link GetId} function.
	 */
	/*@ non_null @*/ public static final GetId getId = new GetId();
	
  /**
   * The trace ID.
   */
  /*@ non_null @*/ protected final Object m_id;
  
  /**
   * The event to append to that trace.
   */
  /*@ non_null @*/ protected final Object m_event;
  
  /**
   * Produces a log update for a trace ID.
   * @param id The trace ID
   * @param event The event to append to that trace
   */
  public LogUpdate(/*@ non_null @*/ Object id, /*@ non_null @*/ Object event)
  {
    super();
    m_id = id;
    m_event = event;
  }
  
  /*@ pure non_null @*/ public Object getId()
  {
    return m_id;
  }
  
  /*@ pure non_null @*/ public Object getEvent()
  {
    return m_event;
  }
  
  @Override
  public int hashCode()
  {
    return m_event.hashCode() + m_id.hashCode();
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof LogUpdate))
    {
      return false;
    }
    LogUpdate u = (LogUpdate) o;
    return m_event.equals(u.m_event) && m_id.equals(u.m_id);
  }

  @Override
  public boolean containsKey(Object key)
  {
    return m_id.equals(key);
  }

  @Override
  public Set<Entry<Object, List<Object>>> entrySet()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Object> keySet()
  {
    HashSet<Object> s = new HashSet<Object>(1);
    s.add(m_id);
    return s;
  }

  @Override
  public List<Object> get(Object key)
  {
    if (m_id.equals(key))
    {
      return Arrays.asList(m_event);
    }
    return null;
  }

  @Override
  public int size()
  {
    return 1;
  }

  @Override
  public Log update(Log log)
  {
    return new Log().update(log);
  }

  @Override
  public boolean isEmpty()
  {
    return false;
  }
  
  @Override
  public String toString()
  {
    return "{" + m_id + "\u21a6" + m_event + "}";
  }
  
  /**
   * A BeepBeep function that extracts the event in a log update.
   */
  public static class GetEvent extends UnaryFunction<LogUpdate,Object>
  {
  	/**
  	 * Creates a new instance of the function. This constructor is not visible
  	 * as a single instance is made available to users.
  	 */
  	protected GetEvent()
  	{
  		super(LogUpdate.class, Object.class);
  	}

		@Override
		public Object getValue(LogUpdate x)
		{
			return x.getEvent();
		}
		
		@Override
		public GetEvent duplicate(boolean with_state)
		{
			return this;
		}
  }
  
  /**
   * A BeepBeep function that extracts the trace identifier in a log update.
   */
  public static class GetId extends UnaryFunction<LogUpdate,Object>
  {
  	/**
  	 * Creates a new instance of the function. This constructor is not visible
  	 * as a single instance is made available to users.
  	 */
  	protected GetId()
  	{
  		super(LogUpdate.class, Object.class);
  	}

		@Override
		public Object getValue(LogUpdate x)
		{
			return x.getId();
		}
		
		@Override
		public GetId duplicate(boolean with_state)
		{
			return this;
		}
  }
}

