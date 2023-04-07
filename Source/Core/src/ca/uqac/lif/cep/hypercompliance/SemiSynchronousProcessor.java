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

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;

/**
 * A n:n processor that does not wait for a complete event front to produce
 * an output event front.
 *  
 * @author Sylvain Hallé
 */
abstract class SemiSynchronousProcessor extends Processor
{
	/**
	 * An array storing the last input event received so far from each input
	 * pipe.
	 */
	/*@ non_null @*/ protected final Object[] m_lasts;
	
	/**
	 * An array storing the state of each input pipe (i.e. {@link true} meaning
	 * that the last event of the pipe has been received, and {@link false}
	 * meaning that further events may come in on this pipe).
	 */
	/*@ non_null @*/ protected final boolean[] m_done;
	
	/**
	 * The array of input pushables for this processor.
	 */
	/*@ non_null @*/ protected final Pushable m_inputPushables[];
	
	/**
	 * The array of output pullables for this processor.
	 */
	/*@ non_null @*/ protected final Pullable m_outputPullables[];
	
	/**
	 * Creates a new instance of the processor with a given input/output arity.
	 * @param arity The arity
	 */
	public SemiSynchronousProcessor(int arity)
	{
		super(arity, arity);
		m_inputPushables = new Pushable[arity];
		m_outputPullables = new Pullable[arity];
		m_lasts = new Object[arity];
		m_done = new boolean[arity];
		for (int i = 0; i < arity; i++)
		{
			m_done[i] = false;
		}
	}
	
	@Override
	public Pushable getPushableInput(int index)
	{
		if (index < 0 || index >= m_inputPushables.length)
		{
			throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds");
		}
		if (m_inputPushables[index] == null)
		{
			m_inputPushables[index] = getNewPushableInput(index);
		}
		return m_inputPushables[index];
	}

	@Override
	public Pullable getPullableOutput(int index)
	{
		if (index < 0 || index >= m_outputPullables.length)
		{
			throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds");
		}
		if (m_outputPullables[index] == null)
		{
			m_outputPullables[index] = getNewPullableOutput(index);
		}
		return m_outputPullables[index];
	}
	
	/**
	 * Checks if all input pipes have produced their last event.
	 * @return {@code true} if this is the case, {@link false} otherwise
	 */
	protected boolean allDone()
	{
		for (boolean b : m_done)
		{
			if (!b)
			{
				return false;
			}
		}
		return true;
	}
	
	protected abstract Pushable getNewPushableInput(int index);
	
	protected abstract Pullable getNewPullableOutput(int index);
}
