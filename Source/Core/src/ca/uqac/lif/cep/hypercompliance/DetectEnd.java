/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2023 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.hypercompliance;

import java.util.Queue;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.util.Equals;

/**
 * Lets all input events through, and sends an "end of trace" signal when a
 * specific event is received.
 * @author Sylvain Hallé
 */
public class DetectEnd extends SynchronousProcessor
{
	/**
	 * A flag that indicates if the end has been detected. The processor will not
	 * output any new event after that.
	 */
	protected boolean m_done;
	
	/**
	 * The object used to signal the last event of a trace.
	 */
	/*@ null @*/ protected final Object m_flag;
	
	/**
	 * Creates a new instance of the processor.
	 * @param o The object used to signal the last event of a trace
	 */
	public DetectEnd(Object o)
	{
		super(1, 1);
		m_flag = o;
		m_done = false;
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		if (m_done)
		{
			return false;
		}
		Object o = inputs[0];
		outputs.add(new Object[] {o});
		m_done = Equals.isEqualTo(m_flag, o);
		return !m_done;
	}
	
	@Override
	public void reset()
	{
		m_done = false;
	}

	@Override
	public DetectEnd duplicate(boolean with_state)
	{
		DetectEnd e = new DetectEnd(m_flag);
		if (with_state)
		{
			e.m_done = m_done;
		}
		return e;
	}
}
