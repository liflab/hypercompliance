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

import java.util.TreeMap;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.PushUnit;
import ca.uqac.lif.cep.tmf.SinkLast;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Maps;

public class Aggregate extends SliceLog
{
	/*@ non_null @*/ protected final Processor m_aggregator;
	
	protected final AggregationPushUnit m_unit;
	
	public Aggregate(/*@ non_null @*/ Processor p, /*@ non_null @*/ Choice c, /*@ non_null @*/ Processor aggregator, AggregationPushUnit apu)
	{
		super(p, c);
		m_aggregator = aggregator;
		m_unit = apu;
	}
	
	public Aggregate(/*@ non_null @*/ Processor p, /*@ non_null @*/ Choice c, /*@ non_null @*/ Processor aggregator, Object[] default_values)
	{
		this(p, c, aggregator, new AggregationPushUnit(aggregator, default_values));
	}
	
	public Aggregate(/*@ non_null @*/ Processor p, /*@ non_null @*/ Choice c, /*@ non_null @*/ Processor aggregator)
	{
		this(p, c, aggregator, new AggregationPushUnit(aggregator));
	}
	
	public Aggregate(/*@ non_null @*/ Processor p, /*@ non_null @*/ Processor aggregator)
	{
		this(p, Choice.ALL, aggregator);
	}
	
	@Override
	protected Object processMap(TreeMap<Object,Object> map)
	{
		m_unit.reset();
		m_unit.push(map);
		return m_unit.getLast();
	}
	
	@Override
	public Aggregate duplicate(boolean with_state)
	{
		Aggregate agg = new Aggregate(m_sliceProcessor, m_choice, m_aggregator, m_unit.duplicate(with_state));
		copyInto(agg, with_state);
		return agg;
	}
	
	protected static class AggregationPushUnit extends PushUnit
	{
		/*@ non_null @*/ protected final Processor m_aggregator;
		
		/*@ null @*/ protected final Object[] m_defaultValues;
		
		protected AggregationPushUnit(Processor p, SinkLast sink, Object[] default_values)
		{
			super(getGroup(p, default_values), sink);
			m_aggregator = p;
			m_defaultValues = default_values;
		}
		
		protected AggregationPushUnit(Processor p, Object[] default_values)
		{
			this(p, new SinkLast(), default_values);
		}
		
		protected AggregationPushUnit(Processor p)
		{
			this(p, null);
		}
		
		protected static GroupProcessor getGroup(Processor p, Object[] default_values)
		{
			GroupProcessor g = new GroupProcessor(1, 1);
			ApplyFunction values = new ApplyFunction(Maps.values);
			RunOn ro = new RunOn(p, default_values);
			Connector.connect(values, ro);
			g.associateInput(0, values, 0);
			g.associateOutput(0, ro, 0);
			g.addProcessors(values, ro);
			return g;
		}
		
		@Override
		public AggregationPushUnit duplicate(boolean with_state)
		{
			AggregationPushUnit apu = new AggregationPushUnit(m_aggregator, m_sink.duplicate(with_state), m_defaultValues);
			copyInto(apu, with_state);
			return apu;
		}
	}

}
