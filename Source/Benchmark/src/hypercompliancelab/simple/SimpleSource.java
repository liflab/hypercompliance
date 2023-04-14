package hypercompliancelab.simple;

import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.synthia.NoMoreElementException;
import ca.uqac.lif.synthia.Picker;
import ca.uqac.lif.synthia.random.RandomBoolean;
import ca.uqac.lif.synthia.random.RandomFloat;
import ca.uqac.lif.synthia.sequence.Knit;
import ca.uqac.lif.synthia.sequence.MarkovChain;
import ca.uqac.lif.synthia.util.Constant;
import ca.uqac.lif.synthia.util.Tick;
import hypercompliancelab.CasePicker;
import hypercompliancelab.LogUpdatePicker;
import hypercompliancelab.PickerSource;

public class SimpleSource extends PickerSource
{
	public static final String NAME = "Simple";
	
	public static final String END = "X";
	
	public SimpleSource(int max_events)
	{
		super(new Knit<LogUpdate>(
				new CasePicker(new Tick(0, 1), new Lifecycle(new RandomFloat())),
				new RandomBoolean(0.05), new RandomBoolean(0.499), new RandomFloat()), max_events);
		
	}
	
	protected static class Lifecycle extends MarkovChain<String>
	{
		public Lifecycle(Picker<Float> source)
		{
			super(source);
			add(0, new Constant<String>(""));
			add(1, new Constant<String>("a"));
			add(2, new Constant<String>("b"));
			add(3, new Constant<String>("c"));
			add(4, new Constant<String>("d"));
			add(5, new Constant<String>(END));
			add(0, 1, 1);
			add(1, 2, 0.5);
			add(1, 3, 0.5);
			add(2, 2, 0.3);
			add(2, 1, 0.5);
			add(2, 4, 0.2);
			add(3, 2, 1);
			add(4, 5, 1);
		}
		
		@Override
		public String pick()
		{
			String s = super.pick();
			if (s == null)
			{
				throw new NoMoreElementException();
			}
			return s;
		}
		
		@Override
		public Lifecycle duplicate(boolean with_state)
		{
			Lifecycle l = new Lifecycle(m_floatSource.duplicate(with_state));
			if (with_state)
			{
				l.m_currentState = m_currentState;
			}
			return l;
		}
	}
	
	public static void main(String[] args)
	{
		SimpleSource l = new SimpleSource(100);
		for (;;)
		{
			System.out.println(l.getPullableOutput().pull());
		}
	}

}
