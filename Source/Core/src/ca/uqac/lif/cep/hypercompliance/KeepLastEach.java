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

import java.util.Iterator;
import java.util.concurrent.Future;

import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;

/**
 * A variation of {@link KeepLast} that creates an output event front out of
 * the last event received from each input pipe <em>separately</em>.
 *   
 * @author Sylvain Hallé
 */
public class KeepLastEach extends SemiSynchronousProcessor
{
	/**
	 * Creates a new instance of the processor with a given input/output arity.
	 * @param arity The arity
	 */
	public KeepLastEach(int arity)
	{
		super(arity);
	}

	/**
	 * Instructs the processor to push the events of its "last" front downstream,
	 * and to indicate that the trace is over.
	 */
	protected void computePush()
	{
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
	
	@Override
	protected KeepLastEachPushable getNewPushableInput(int index)
	{
		return new KeepLastEachPushable(index);
	}

	@Override
	protected Pullable getNewPullableOutput(int index)
	{
		return new KeepLastEachPullable(index);
	}

	/**
	 * The implementation of {@link Pushable} specific to this processor. 
	 */
	protected class KeepLastEachPushable implements Pushable
	{
		/**
		 * The index of the input pipe this pushable is linked with.
		 */
		protected final int m_index;

		/**
		 * Creates a new instance of pushable.
		 * @param index The index of the input pipe this pushable is linked with
		 */
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
				computePush();
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

	/**
	 * The implementation of {@link Pullable} specific to this processor.
	 * <p>
	 * <strong>Caveat emptor:</strong> pull behavior is not completely
	 * implemented for this processor at the moment.
	 */
	protected class KeepLastEachPullable implements Pullable
	{
		/**
		 * The index of the output pipe this pullable is linked with.
		 */
		protected final int m_index;

		/**
		 * Creates a new instance of pullable.
		 * @param index The index of the output pipe this pullable is linked with
		 */
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
