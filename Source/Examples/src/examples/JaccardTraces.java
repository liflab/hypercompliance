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
package examples;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.BinaryFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.KeepLastEach;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.LogSource;
import ca.uqac.lif.cep.hypercompliance.Quantify;
import ca.uqac.lif.cep.hypercompliance.Quantify.QuantifierType;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.ltl.HardCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tmf.Pump;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.cep.util.Sets;

import static ca.uqac.lif.cep.Connector.connect;

/**
 * Evaluates the hyperpolicy: "for any two traces, the Jaccard index of the
 * sets of events they contain is greater than ½".
 */
public class JaccardTraces
{
	public static void main(String[] args)
	{
		Log log = new Log();
		//log.put(0, Arrays.asList("a", "b", "a", "c", "a"));
		//log.put(1, Arrays.asList("c", "b", "d", "c", "b"));
		log.put(0, Arrays.asList("a", "b", "c"));
		log.put(1, Arrays.asList("c", "b", "a"));
		log.put(2, Arrays.asList("z", "y"));
		LogSource source = new LogSource(log);
		GroupProcessor jaccard = new GroupProcessor(2, 1);
		{
			Sets.PutInto put1 = new Sets.PutInto();
			Sets.PutInto put2 = new Sets.PutInto();
			KeepLastEach k = new KeepLastEach(2);
			connect(put1, 0, k, 0);
			connect(put2, 0, k, 1);
			ApplyFunction j = new ApplyFunction(JaccardIndex.instance);
			connect(k, j);
			Fork f = new Fork();
			connect(j, f);
			ApplyFunction gt = new ApplyFunction(new FunctionTree(HardCast.instance, Numbers.isGreaterOrEqual));
			connect(f, 0, gt, 0);
			TurnInto fraction = new TurnInto(0.5);
			connect(f, 1, fraction, 0);
			connect(fraction, 0, gt, 1);
			jaccard.associateInput(0, put1, 0);
			jaccard.associateInput(1, put2, 0);
			jaccard.associateOutput(0, gt, 0);
			jaccard.addProcessors(put1, put2, k, j, f, fraction, gt);
		}
		Quantify q = new Quantify(jaccard, true, QuantifierType.ALL, QuantifierType.ALL);
		Pump p = new Pump();
		connect(source, p, q, new Print().setPrefix("!"));
		p.start();
	}
	
	/**
	 * Function that calculates the
	 * <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard index</a> of
	 * two sets.
	 */
	@SuppressWarnings("rawtypes")
	public static class JaccardIndex extends BinaryFunction<Set,Set,Float>
	{
		public static final JaccardIndex instance = new JaccardIndex();
		
		protected JaccardIndex()
		{
			super(Set.class, Set.class, Float.class);
		}

		@Override
		public Float getValue(Set x, Set y)
		{
			System.out.println(x + "," + y);
			float inter = 0, union = 0;
			for (Object o : x)
			{
				union++;
				if (y.contains(o))
				{
					inter++;
				}
			}
			for (Object o : y)
			{
				if (!x.contains(o))
				{
					union++;
				}
			}
			if (union == 0)
			{
				return 0f;
			}
			return inter / union;
		}
	}
}
