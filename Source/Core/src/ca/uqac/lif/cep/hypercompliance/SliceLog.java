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

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.tmf.PushUnit;
import ca.uqac.lif.cep.tmf.SinkLast;

/**
 * A processor that receives log update events, and evaluates the result of a
 * distinct instance of a given processor &pi; for each trace identifier. The
 * results for each trace are aggregated into a map associating trace
 * identifiers with the last event produced by the corresponding processor
 * instance.
 * <p>
 * As an example, suppose that &pi; is the processor producing the cumulative
 * sum of a stream of numbers, and the following input stream:
 * <p>
 * {a &mapsto; 1}, {a &mapsto; 2}, {b &mapsto; 8}, &hellip;
 * <p>
 * The {@code SliceLog} processor would produce as its output:
 * <p>
 * {a &mapsto; 1}, {a &mapsto; 3}, {a &mapsto; 3, b &mapsto; 8}, &hellip; 
 * @author Sylvain Hallé
 */
public class SliceLog extends SynchronousProcessor
{
	/**
	 * Determines what traces should be considered when building the output
	 * map.
	 */
	public static enum Choice {ACTIVE, INACTIVE, ALL}

	/**
	 * The processor to run on each trace.
	 */
	/*@ non_null @*/ protected final Processor m_sliceProcessor;

	/**
	 * A map associating trace identifiers with their corresponding
	 * {@link SlicePushUnit}.
	 */
	/*@ non_null @*/ protected final Map<Object,SlicePushUnit> m_slices;

	/**
	 * Determines what traces should be considered when building the output
	 * map.
	 */
	/*@ non_null @*/ protected final Choice m_choice;
	
	/*@ null @*/ protected Object m_lastOutput;

	/**
	 * Creates a new slice log processor.
	 * @param p The processor to run on each trace
	 */
	public SliceLog(/*@ non_null @*/ Processor p)
	{
		this(p, Choice.ALL);
	}

	/**
	 * Creates a new slice log processor.
	 * @param p The processor to run on each trace
	 * @param c Determines what traces should be considered when building the
	 * output map
	 */
	public SliceLog(/*@ non_null @*/ Processor p, Choice c)
	{
		super(1, 1);
		m_sliceProcessor = p;
		m_slices = new TreeMap<Object,SlicePushUnit>();
		m_lastOutput = null;
		m_choice = c;
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
		{
			SlicePushUnit spu = m_slices.get(trace_id);
			boolean active_before = spu.isActive();
			spu.push(upd.getEvent());
			boolean active_after = spu.isActive();
			if (m_choice == Choice.INACTIVE && !(active_before == true && active_after == false))
			{
				// No point in recalculating, just output the last map
				if (m_lastOutput != null)
				{
					outputs.add(new Object[] {m_lastOutput});
				}
				return true;
			}
		}
		TreeMap<Object,Object> out_map = new TreeMap<Object,Object>();
		Iterator<Map.Entry<Object,SlicePushUnit>> it = m_slices.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<Object,SlicePushUnit> entry = it.next();
			SlicePushUnit spu = entry.getValue();
			if (!spu.isActive() && m_choice == Choice.ACTIVE)
			{
				it.remove();
				continue;
			}
			if (isChosen(spu))
			{
				Object last = spu.getLast();
				if (last != null)
				{
					out_map.put(entry.getKey(), spu.getLast());
				}
			}
		}
		Object o = processMap(out_map);
		//System.out.println(m_slices.size());
		if (o != null)
		{
			outputs.add(new Object[] {o});
			m_lastOutput = o;
		}
		return true;
	}
	
	protected Object processMap(TreeMap<Object,Object> map)
	{
		return map;
	}
	
	/**
	 * Determines if a slice push unit should be included in the values of the
	 * output map produced by the processor. A push unit is considered if:
	 * <ul>
	 * <li>the choice parameter is set to {@link Choice#ALL}, or</li>
	 * <li>the choice parameter is set to {@link Choice#ACTIVE} and the push
	 * unit is active, or</li>
	 * <li>the choice parameter is set to {@link Choice#INACTIVE} and the push
	 * unit is inactive</li>
	 * </ul>
	 * @param spu The unit
	 * @return {@link true} if the push unit should be considered, {@link false}
	 * otherwise
	 */
	protected boolean isChosen(SlicePushUnit spu)
	{
		boolean active = spu.isActive();
		return m_choice == Choice.ALL || (active && m_choice == Choice.ACTIVE)
				|| (!active && m_choice == Choice.INACTIVE);
	}

	@Override
	public SliceLog duplicate(boolean with_state)
	{
		SliceLog s = new SliceLog(m_sliceProcessor);
		copyInto(s, with_state);
		return s;
	}
	
	protected void copyInto(SliceLog s, boolean with_state)
	{
		if (with_state)
		{
			for (Map.Entry<Object,SlicePushUnit> e : m_slices.entrySet())
			{
				s.m_slices.put(e.getKey(), e.getValue().duplicate(with_state));
			}
		}
	}

	/**
	 * A {@link PushUnit} specific to the handling of trace slices. This push
	 * unit simply pushes events to the enclosed processor, but also keeps track
	 * of the "end of trace" notification to determine if the slice is active
	 * or not.
	 */
	protected class SlicePushUnit extends PushUnit
	{
		/**
		 * The flag that keeps track of the activity of the push unit.
		 */
		protected boolean m_isActive;

		public SlicePushUnit(Processor p)
		{
			this(p, new SinkLast());
		}

		public SlicePushUnit(Processor p, SinkLast sink)
		{
			super(p, sink);
			m_isActive = true;
		}

		public boolean isActive()
		{
			return !m_sink.seenEndOfTrace();
		}
		
		@Override
		public void push(Object o)
		{
			super.push(o);
			if (o == null || m_sink.seenEndOfTrace())
			{
				// We set these to null to "delete" them
				m_processor = null;
				m_pushable = null;
				m_isActive = false;
			}
		}

		@Override
		public SlicePushUnit duplicate(boolean with_state)
		{
			SlicePushUnit spu = new SlicePushUnit(m_processor.duplicate(with_state), m_sink.duplicate(with_state));
			copyInto(spu, with_state);
			return spu;
		}

		protected void copyInto(SlicePushUnit spu, boolean with_state)
		{
			super.copyInto(spu, with_state);
			if (with_state)
			{
				spu.m_isActive = m_isActive;
			}
		}
	}
}
