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

import java.util.Queue;

import ca.uqac.lif.cep.Processor;

public class SliceLogLast extends SliceLog
{
	public SliceLogLast(Processor p)
	{
		super(p);
	}

	public SliceLogLast(Processor p, Choice c)
	{
		super(p, c);
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		LogUpdate upd = (LogUpdate) inputs[0];
		Object trace_id = upd.getId();
		if (!m_slices.containsKey(trace_id))
		{
			m_slices.put(trace_id, new SlicePushUnit(m_sliceProcessor.duplicate()));
		}
		SlicePushUnit spu = m_slices.get(trace_id);
		Object ev = upd.getEvent();
		spu.push(ev);
		Object o = spu.getLast();
		if (o != null)
		{
			outputs.add(new Object[] {o});
		}
		return true;
	}
}
