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
import hypercompliancelab.PickerSource;

/**
 * A simple source producing sequences of abstract events labelled "a", "b",
 * "c" and "d" according to random walks in a
 * {@link MarkovChain Markov chain}.
 * <p>
 * The Markov chain is illustrated below:
 * <p>
 * <img src="{@docRoot}/doc-files/SimpleSource.png" alt="Markov chain"/>
 *  
 * @author Sylvain Hallé
 */
public class SimpleSource extends PickerSource
{
	public static final String NAME = "Simple";

	public static final String END = "X";

	public SimpleSource(int max_events, int seed)
	{
		super(new Knit<LogUpdate>(
				new CasePicker(new Tick(0, 1), new Lifecycle(new RandomFloat().setSeed(seed))),
				new RandomBoolean(0.05).setSeed(seed + 1), 
				new RandomBoolean(0.499).setSeed(seed + 2), 
				new RandomFloat().setSeed(seed + 3)), 
				max_events);
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
		SimpleSource l = new SimpleSource(100, 0);
		for (;;)
		{
			System.out.println(l.getPullableOutput().pull());
		}
	}

}
