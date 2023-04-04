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
  public boolean containsKey(Object key);
  
  public Set<Entry<Object, List<Object>>> entrySet();
  
  public Set<Object> keySet();
  
  public List<Object> get(Object key);
  
  public int size();
  
  /**
   * Updates the content of a log with additions defined in another log.
   * @param log The additions
   * @return The resulting log
   */
  public Log update(Log log);
  
  public boolean isEmpty();
}
