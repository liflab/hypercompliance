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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.tmf.SinkLast;

public class AggregateSequence extends SliceLog
{
	/**
	 * A push unit to evaluate the pre-order processor.
	 */
	protected final SlicePushUnit m_preorderUnit;

	/**
	 * The processor that handles the pre-order stream on each slice.
	 */
	protected final Processor m_preorderSlice;

	/**
	 * A push unit to evaluate the aggregation processor.
	 */
	protected final SlicePushUnit m_aggregationUnit;

	/**
	 * A map associating slice IDs to comparable push units.
	 */
	protected final Map<Object,ComparablePushUnit> m_orderInstances;

	/**
	 * A list used to sort comparable push units.
	 */
	protected final List<ComparablePushUnit> m_sortedInstances;
	
	public AggregateSequence(/*@ non_null @*/ Processor value_p, /*@ non_null @*/ Choice c, Processor preorder_p, SlicePushUnit preorder_pu, SlicePushUnit aggregator_pu)
	{
		super(value_p, c);
		m_preorderSlice = preorder_p;
		m_aggregationUnit = aggregator_pu;
		m_preorderUnit = preorder_pu;
		m_sortedInstances = new ArrayList<ComparablePushUnit>();
		m_orderInstances = new HashMap<Object,ComparablePushUnit>();
	}

	public AggregateSequence(/*@ non_null @*/ Processor value_p, /*@ non_null @*/ Choice c, Processor preorder, Processor preorder_p, /*@ non_null @*/ Processor aggregator)
	{
		super(value_p, c);
		m_preorderSlice = preorder_p;
		m_aggregationUnit = new SlicePushUnit(aggregator);
		m_preorderUnit = new SlicePushUnit(preorder);
		m_sortedInstances = new ArrayList<ComparablePushUnit>();
		m_orderInstances = new HashMap<Object,ComparablePushUnit>();
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		LogUpdate upd = (LogUpdate) inputs[0];
		Object trace_id = upd.getId();
		if (!m_slices.containsKey(trace_id))
		{
			m_slices.put(trace_id, new SlicePushUnit(m_sliceProcessor.duplicate()));
			ComparablePushUnit cpu = new ComparablePushUnit(trace_id, m_preorderSlice.duplicate());
			m_orderInstances.put(trace_id, cpu);
			m_sortedInstances.add(cpu);
		}
		{
			Object event = upd.getEvent();
			m_preorderUnit.push(event);
			SlicePushUnit spu = m_slices.get(trace_id);
			boolean active_before = spu.isActive();
			if (spu == null || !spu.isActive())
			{
				throw new ProcessorException("Attempting to push event to a completed slice: " + trace_id);
			}
			else
			{
				spu.push(event);
			}
			boolean active_after = spu.isActive();
			Object preorder_last = m_preorderUnit.getLast();
			if (preorder_last != null)
			{
				ComparablePushUnit cpu = m_orderInstances.get(trace_id);
				cpu.push(preorder_last);
				if (active_before && !active_after)
				{
					cpu.notifyEndOfTrace();
				}
			}
		}
		Collections.sort(m_sortedInstances);
		m_aggregationUnit.reset();
		Iterator<ComparablePushUnit> it = m_sortedInstances.iterator();
		while (it.hasNext())
		{
			ComparablePushUnit cpu = it.next();
			Object t_id = cpu.getTraceId();
			SlicePushUnit spu = m_slices.get(t_id);
			if (!spu.isActive() && m_choice == Choice.ACTIVE)
			{
				m_slices.remove(t_id);
				m_orderInstances.remove(t_id);
				it.remove();
				continue;
			}
			if (isChosen(spu))
			{
				Object last = spu.getLast();
				if (last != null)
				{
					m_aggregationUnit.push(last);
					//System.out.print(last);
				}
			}
		}
		//System.out.println();
		Object agg_last = m_aggregationUnit.getLast();
		if (agg_last != null)
		{
			outputs.add(new Object[] {agg_last});
		}
		return true;
	}

	@Override
	public AggregateSequence duplicate(boolean with_state)
	{
		AggregateSequence agg = new AggregateSequence(m_sliceProcessor, m_choice, m_preorderSlice, m_preorderUnit.duplicate(with_state), m_aggregationUnit.duplicate(with_state));
		copyInto(agg, with_state);
		return agg;
	}
	
	protected void copyInto(AggregateSequence agg, boolean with_state)
	{
		super.copyInto(agg, with_state);
		if (with_state)
		{
			for (Map.Entry<Object,ComparablePushUnit> e : m_orderInstances.entrySet())
			{
				ComparablePushUnit cpu = e.getValue().duplicate(with_state);
				agg.m_orderInstances.put(e.getKey(), cpu);
				agg.m_sortedInstances.add(cpu);
			}
		}
	}

	/**
	 * A {@link SlicePushUnit} that can be compared with another one.
	 */
	protected static class ComparablePushUnit extends SlicePushUnit implements Comparable<ComparablePushUnit>
	{		
		protected final Object m_traceId;

		protected ComparablePushUnit(Object trace_id, Processor p, SinkLast sink)
		{
			super(p, sink);
			m_traceId = trace_id;
		}

		protected ComparablePushUnit(Object trace_id, Processor p)
		{
			this(trace_id, p, new SinkLast());
		}

		public Object getTraceId()
		{
			return m_traceId;
		}
		
		public void notifyEndOfTrace()
		{
			m_pushable.notifyEndOfTrace();
		}
		
		@Override
		public String toString()
		{
			return m_traceId + "->" + getLast();
		}

		@Override
		public ComparablePushUnit duplicate(boolean with_state)
		{
			ComparablePushUnit cpu = new ComparablePushUnit(m_processor.duplicate(with_state), m_sink.duplicate(with_state));
			copyInto(cpu, with_state);
			return cpu;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compareTo(ComparablePushUnit cpu)
		{
			Object o1 = getLast();
			if (o1 == null)
			{
				return 0;
			}
			Object o2 = cpu.getLast();
			if (o1 instanceof Comparable && o2 != null)
			{
				return ((Comparable) o1).compareTo(o2);
			}
			return 0;
		}
	}

}
