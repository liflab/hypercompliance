/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2016 Sylvain Hallé

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

import java.util.Iterator;
import java.util.concurrent.Future;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;

public class KeepLastEach extends Processor
{
	protected final Object[] m_lasts;
	
	protected final boolean[] m_done;
	
	protected final KeepLastEachPushable m_inputPushables[];
	
	protected final KeepLastEachPullable m_outputPullables[];
	
	public KeepLastEach(int arity)
	{
		super(arity, arity);
		m_inputPushables = new KeepLastEachPushable[arity];
		m_outputPullables = new KeepLastEachPullable[arity];
		m_lasts = new Object[arity];
		m_done = new boolean[arity];
		for (int i = 0; i < arity; i++)
		{
			m_done[i] = false;
		}
	}
	
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

	@Override
	public Pushable getPushableInput(int index)
	{
		if (index < 0 || index >= m_inputPushables.length)
		{
			throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds");
		}
		if (m_inputPushables[index] == null)
		{
			m_inputPushables[index] = new KeepLastEachPushable(index);
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
			m_outputPullables[index] = new KeepLastEachPullable(index);
		}
		return m_outputPullables[index];
	}
	
	protected void compute()
	{
		System.out.println("Compute");
		for (int i = 0; i < m_lasts.length; i++)
		{
			m_outputPushables[i].push(m_lasts[i]);
			m_outputPushables[i].notifyEndOfTrace();
		}
	}

	@Override
	public KeepLastEach duplicate(boolean with_state)
	{
		KeepLastEach k = new KeepLastEach(getInputArity());
		if (with_state)
		{
			for (int i = 0; i < m_lasts.length; i++)
			{
				k.m_lasts[i] = m_lasts[i];
				k.m_done[i] = m_done[i];
			}
		}
		return k;
	}
	
	protected class KeepLastEachPushable implements Pushable
	{
		protected final int m_index;
		
		public KeepLastEachPushable(int index)
		{
			super();
			m_index = index;
		}
		
		@Override
		public Pushable push(Object o)
		{
			m_lasts[m_index] = o;
			return this;
		}

		@Override
		public Future<Pushable> pushFast(Object o)
		{
			push(o);
      return Pushable.NULL_FUTURE;
		}

		@Override
		public void notifyEndOfTrace() throws PushableException
		{
			m_done[m_index] = true;
			if (allDone())
			{
				compute();
			}
		}

		@Override
		public KeepLastEach getProcessor()
		{
			return KeepLastEach.this;
		}

		@Override
		public int getPosition()
		{
			return m_index;
		}
	}
	
	protected class KeepLastEachPullable implements Pullable
	{
		protected final int m_index;
		
		public KeepLastEachPullable(int index)
		{
			super();
			m_index = index;
		}
		
		@Override
		public Object pull()
		{
			// TODO
			return null;
		}


		@Override
		public KeepLastEach getProcessor()
		{
			return KeepLastEach.this;
		}

		@Override
		public int getPosition()
		{
			return m_index;
		}

		@Override
		public Iterator<Object> iterator()
		{
			return this;
		}

		@Override
		public Object pullSoft()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object next()
		{
			return pull();
		}

		@Override
		public NextStatus hasNextSoft()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasNext()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void start()
		{
			// TODO
			
		}

		@Override
		public void stop()
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void dispose()
		{
			// TODO Auto-generated method stub
		}
	}
}
