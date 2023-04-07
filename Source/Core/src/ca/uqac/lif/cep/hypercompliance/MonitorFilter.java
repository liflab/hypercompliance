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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.SinkLast;

public class MonitorFilter extends SynchronousProcessor
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
	 * The list of events pushed to this processor and that have not been
	 * released yet.
	 */
	/*@ non_null @*/ protected final List<Object> m_retained;

	/**
	 * A flag indicating if the events pushed to this processor are allowed to
	 * be let through.
	 */
	/*@ non_null @*/ protected Troolean.Value m_verdict;

	/**
	 * Creates a new push unit.
	 */
	public MonitorFilter(Processor p)
	{
		this(p, new SinkLast());
	}

	protected MonitorFilter(Processor p, SinkLast sink)
	{
		super(1, 1);
		m_processor = p;
		m_sink = sink;
		Connector.connect(m_processor, m_sink);
		m_pushable = m_processor.getPushableInput();
		m_retained = new ArrayList<Object>();
		m_verdict = Troolean.Value.INCONCLUSIVE;
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		if (m_verdict == Troolean.Value.FALSE)
		{
			// Don't bother
			return false;
		}
		if (m_verdict == Troolean.Value.TRUE)
		{
			outputs.add(new Object[] {inputs[0]});
			return true;
		}
		m_pushable.push(inputs[0]);
		List<Object> to_output = new ArrayList<Object>();
		m_verdict = (Troolean.Value) getLast();
		if (m_verdict == Troolean.Value.FALSE)
		{
			m_retained.clear();
			return false;
		}
		else if (m_verdict == Troolean.Value.TRUE)
		{
			to_output.addAll(m_retained);
			m_retained.clear(); 
		}
		else
		{
			m_retained.add(inputs[0]);
		}
		if (m_verdict == Troolean.Value.TRUE)
		{
			to_output.add(inputs[0]);
		}
		for (Object o : to_output)
		{
			outputs.add(new Object[] {o});
		}
		return true;
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
	public MonitorFilter duplicate(boolean with_state)
	{
		MonitorFilter mc = new MonitorFilter(m_processor.duplicate(with_state), m_sink.duplicate(with_state));
		if (with_state)
		{
			mc.m_verdict = m_verdict;
			mc.m_retained.addAll(m_retained);
		}
		return mc;
	}
}
