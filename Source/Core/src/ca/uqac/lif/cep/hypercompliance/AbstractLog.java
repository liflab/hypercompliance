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
import java.util.Set;
import java.util.Map.Entry;

/**
 * An association between stream identifiers and streams. Logs can be queried
 * in a manner similar to a {@link Map}.
 */
public interface AbstractLog
{
  /**
   * Determines if a log contains a trace for a given identifier.
   * @param key The identifier
   * @return {@code true} if the log contains a trace for this identifier,
   * {@code false} otherwise
   */
  public boolean containsKey(Object key);
  
  /**
   * Gets the set of entries in this log. In this context, an entry's key is
   * a trace identifier, and its associated value is the corresponding trace
   * of events.
   * @return The set of entries 
   */
  public Set<Entry<Object, List<Object>>> entrySet();
  
  /**
   * Gets the set of trace identifiers that this log contains.
   * @return The set of identifiers
   */
  public Set<Object> keySet();
  
  /**
   * Gets the trace of events associated to a given trace identifier.
   * @param key The identifier
   * @return The corresponding trace of events, or {@code null} if no trace
   * exists for this identifier
   */
  public List<Object> get(Object key);
  
  /**
   * Gets the size of this log, expressed in terms of the number of traces
   * it contains.
   * @return The log size
   */
  public int size();
  
  /**
   * Updates the content of a log with additions defined in another log.
   * @param log The additions
   * @return The resulting log
   */
  public Log update(Log log);
  
  /**
   * Determines if the log is empty, i.e.<!-- --> does not contain any trace.
   * @return {@code true} if the log is empty, {@code false} otherwise
   */
  public boolean isEmpty();
}
