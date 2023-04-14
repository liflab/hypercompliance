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

import ca.uqac.lif.synthia.Picker;

public class CasePicker implements Picker<LogUpdatePicker>
{
	protected final Picker<?> m_caseIdPicker;
	
	protected final Picker<?> m_picker;
	
	public CasePicker(Picker<?> case_id_picker, Picker<?> event_picker)
	{
		super();
		m_caseIdPicker = case_id_picker;
		m_picker = event_picker;
	}

	@Override
	public CasePicker duplicate(boolean with_state)
	{
		return new CasePicker(m_caseIdPicker.duplicate(with_state), m_picker.duplicate(with_state));
	}

	@Override
	public LogUpdatePicker pick()
	{
		Object case_id = m_caseIdPicker.pick();
		return new LogUpdatePicker(case_id, m_picker.duplicate(false));
	}

	@Override
	public void reset()
	{
		m_caseIdPicker.reset();
		m_picker.reset();
	}
}
