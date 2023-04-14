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
package hypercompliancelab;

import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.synthia.NoMoreElementException;
import ca.uqac.lif.synthia.Picker;

/**
 * A picker that wraps events produced by another picker into a
 * {@link LogUpdate} event for a given case ID.
 * 
 * @author Sylvain Hallé
 */
public class LogUpdatePicker implements Picker<LogUpdate>
{
	protected final Picker<?> m_eventPicker;
	
	protected final Object m_caseId;
	
	public LogUpdatePicker(Object case_id, Picker<?> event_picker)
	{
		super();
		m_caseId = case_id;
		m_eventPicker = event_picker;
	}
	
	/**
	 * Gets a new instance of log udpate picker for a new case ID.
	 * @param case_id The case ID
	 * @return The new picker instance
	 */
	public LogUpdatePicker newInstance(Object case_id)
	{
		return new LogUpdatePicker(case_id, m_eventPicker.duplicate(false));
	}

	@Override
	public LogUpdatePicker duplicate(boolean with_state)
	{
		return new LogUpdatePicker(m_caseId, m_eventPicker.duplicate(with_state));
	}

	@Override
	public LogUpdate pick() throws NoMoreElementException
	{
		Object e = m_eventPicker.pick();
		return new LogUpdate(m_caseId, e);
	}

	@Override
	public void reset()
	{
		m_eventPicker.reset();
	}
}
