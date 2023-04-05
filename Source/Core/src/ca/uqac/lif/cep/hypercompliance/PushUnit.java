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

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Duplicable;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * The encapsulation of a processor connected to a sink. This is a utility
 * class used by other processors such as {@link FilterLogs} and
 * {@link LogWindow}.
 */
class PushUnit implements Duplicable
{
	/**
   * The processor to which events are pushed.
   */
  /*@ non_null @*/ protected final Processor m_processor;

  /**
   * The pushable object to push events to this processor.
   */
  /*@ non_null @*/ protected final Pushable m_pushable;

  /**
   * The sink that collects events pushed to the processor.
   */
  /*@ non_null @*/ protected final SinkLast m_sink;
  
  /**
   * Creates a new push unit.
   */
  public PushUnit(Processor p)
  {
  	this(p, new SinkLast());
  }
  
  protected PushUnit(Processor p, SinkLast sink)
  {
  	super();
  	m_processor = p;
  	m_sink = sink;
  	Connector.connect(m_processor, m_sink);
  	m_pushable = m_processor.getPushableInput();
  }
  
  /**
   * Pushes an event into the push unit, or notifies the processor that the
   * input trace is over.
   * @param o An event, or {@code null} to indicate that no further event will
   * be pushed
   */
  public void push(Object o)
  {
  	if (o == null)
  	{
  		m_pushable.notifyEndOfTrace();
  	}
  	else
  	{
  		m_pushable.push(o);
  	}
  }
  
  public Object getLast()
  {
  	Object[] o = m_sink.getLast();
  	if (o == null)
  	{
  		return null;
  	}
  	return o[0];
  }

	@Override
	public PushUnit duplicate()
	{
		return duplicate(false);
	}

	@Override
	public PushUnit duplicate(boolean with_state)
	{
		PushUnit pu = new PushUnit(m_processor.duplicate(with_state), m_sink.duplicate(with_state));
		copyInto(pu, with_state);
		return pu;
	}
	
	protected void copyInto(PushUnit pu, boolean with_state)
	{
		// Nothing to do
	}
}
