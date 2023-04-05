package ca.uqac.lif.cep.hypercompliance;

import java.util.TreeMap;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.tmf.SinkLast;
import ca.uqac.lif.cep.util.Bags.RunOn;
import ca.uqac.lif.cep.util.Maps;

public class Aggregate extends SliceLog
{
	/*@ non_null @*/ protected final Processor m_aggregator;
	
	protected final AggregationPushUnit m_unit;
	
	public Aggregate(/*@ non_null @*/ Processor p, /*@ non_null @*/ Choice c, /*@ non_null @*/ Processor aggregator)
	{
		super(p, c);
		m_aggregator = aggregator;
		m_unit = new AggregationPushUnit(aggregator);
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
	
	protected static class AggregationPushUnit extends PushUnit
	{
		protected AggregationPushUnit(Processor p, SinkLast sink)
		{
			super(getGroup(p), sink);
		}
		
		protected AggregationPushUnit(Processor p)
		{
			super(getGroup(p), new SinkLast());
		}
		
		protected static GroupProcessor getGroup(Processor p)
		{
			GroupProcessor g = new GroupProcessor(1, 1);
			ApplyFunction values = new ApplyFunction(Maps.values);
			RunOn ro = new RunOn(p);
			Connector.connect(values, ro);
			g.associateInput(0, values, 0);
			g.associateOutput(0, ro, 0);
			g.addProcessors(values, ro);
			return g;
		}
	}

}
