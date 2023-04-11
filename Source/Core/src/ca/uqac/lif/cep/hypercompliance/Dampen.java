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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.ltl.Troolean.Value;

/**
 * Returns {@link Troolean.Value.FALSE} if <i>k</i> of the last <i>n</i>
 * previous events were false, and {@link Troolean.Value.INCONCLUSIVE}
 * otherwise. As a result, this processor has the effect of "dampening" a
 * condition by allowing it to be temporarily violated.
 * @author Sylvain Hallé
 */
public class Dampen extends SynchronousProcessor
{
	/**
	 * The number of successive {@code false} events that trigger the output of
	 * {@code false}.
	 */
	protected final int m_threshold;
	
	/**
	 * The width of the window.
	 */
	protected final int m_width;
	
	/**
	 * The window of past values.
	 */
	/*@ non_null @*/ protected final Deque<Value> m_window;

	/**
	 * The number of false events currently in the window. 
	 */
	protected int m_falseCount;

	/**
	 * 
	 * @param threshold The number of {@code false} events that
	 * trigger the output of {@code false}
	 * @param width The width of the window
	 */
	public Dampen(int threshold, int width)
	{
		super(1, 1);
		m_falseCount = 0;
		m_threshold = threshold;
		m_width = width;
		m_window = new ArrayDeque<Value>(width);
	}
	
	/**
	 * Gets the current number of false events in the window. This method is
	 * mostly used for testing and debugging.
	 * @return The number of false events
	 */
	int getFalseCount()
	{
		return m_falseCount;
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		Value v = (Value) inputs[0];
		if (v == Value.FALSE)
		{
			m_falseCount++;
		}
		m_window.add(v);
		int w_size = m_window.size();
		if (w_size < m_width)
		{
			outputs.add(new Object[] {Value.INCONCLUSIVE});
			return true;
		}
		if (w_size > m_width)
		{
			v = m_window.removeFirst();
			if (v == Value.FALSE)
			{
				m_falseCount--;
			}
		}
		if (m_falseCount >= m_threshold)
		{
			outputs.add(new Object[] {Value.FALSE});
		}
		else
		{
			outputs.add(new Object[] {Value.INCONCLUSIVE});
		}
		return true;
	}

	@Override
	public Dampen duplicate(boolean with_state)
	{
		Dampen d = new Dampen(m_threshold, m_width);
		if (with_state)
		{
			d.m_falseCount = m_falseCount;
			d.m_window.addAll(m_window);
		}
		return d;
	}
	
	@Override
	public String toString()
	{
		return "Dampen(" + m_threshold + ")";
	}
}
