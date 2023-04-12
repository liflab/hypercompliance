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

import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * A variant of {@link SinkLast} that clears its memory when it receives the
 * end of trace signal. This is used by {@link SliceLog} to determine which
 * slices are active.
 * @author Sylvain Hallé
 */
class SinkLastClear extends SinkLast
{
	/**
	 * The flag that remembers if the end of trace has been seen.
	 */
	protected boolean m_seenEndOfTrace;
	
	/**
   * Creates a new sink last processor
   */
  public SinkLastClear()
  {
    this(1);
  }
  
  /**
   * Creates a new sink last processor of given input arity
   * @param in_arity The input arity
   */
  public SinkLastClear(int in_arity)
  {
    super(in_arity);
    m_seenEndOfTrace = false;
  }
  
  @Override
  protected boolean onEndOfTrace(Queue<Object[]> outputs) throws ProcessorException
  {
  	super.onEndOfTrace(outputs);
  	m_seenEndOfTrace = true;
  	return false;
  }
  
  @Override
  public SinkLastClear duplicate(boolean with_state)
  {
  	SinkLastClear s = new SinkLastClear(getInputArity());
  	if (with_state)
  	{
  		s.m_seenEndOfTrace = m_seenEndOfTrace;
  		for (int i = 0; i < m_last.length; i++)
    	{
    		s.m_last[i] = m_last[i];
    	}
  	}
  	return s;
  }
  
  /**
   * Queries if the processor has seen the end of the input trace.
   * @return {@code true} if the end of trace signal has been received,
   * {@code false} otherwise
   */
  /*@ pure @*/ public boolean seenEndOfTrace()
  {
  	return m_seenEndOfTrace;
  }
}
