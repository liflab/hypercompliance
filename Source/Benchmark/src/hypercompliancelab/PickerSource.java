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

import java.util.Queue;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.tmf.Source;
import ca.uqac.lif.synthia.NoMoreElementException;
import ca.uqac.lif.synthia.Picker;

/**
 * A source of {@link LogUpdate} events produced by a {@link Picker}.
 * @author Sylvain Hallé
 */
public class PickerSource extends Source
{
	protected final Picker<LogUpdate> m_picker;
	
	protected int m_numEvents;
	
	protected int m_maxEvents;
	
	public PickerSource(Picker<LogUpdate> picker, int max_events)
	{
		super(1);
		m_picker = picker;
		m_numEvents = 0;
		m_maxEvents = max_events;
	}
	
	public PickerSource(Picker<LogUpdate> picker)
	{
		this(picker, -1);
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		if (m_maxEvents > 0)
		{
			if (m_numEvents > m_maxEvents)
			{
				return false;
			}
			else
			{
				m_numEvents++;
			}
		}
		try
		{
			LogUpdate lu = m_picker.pick();
			outputs.add(new Object[] {lu});
		}
		catch (NoMoreElementException e)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		m_picker.reset();
		m_numEvents = 0;
	}

	@Override
	public Processor duplicate(boolean with_state)
	{
		throw new UnsupportedOperationException();
	}

}
