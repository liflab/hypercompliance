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

import ca.uqac.lif.cep.tmf.QueueSource;

/**
 * A source of {@link LogUpdate} events obtained from a {@link Log}.
 * 
 * @author Sylvain Hallé
 */
public class LogSource extends QueueSource
{
	/**
   * Creates a new log source.
   * @param log The original log containing several non-interleaved instances
   * of a process
   */
	public LogSource(Log log)
	{
		super(1);
		loop(false);
		for (Map.Entry<Object,List<Object>> e : log.entrySet())
		{
			for (Object ev : e.getValue())
			{
				m_events.add(new LogUpdate(e.getKey(), ev));
			}
			m_events.add(new LogUpdate(e.getKey(), null));
		}
	}
	
	protected LogSource()
	{
		super(1);
	}
}
