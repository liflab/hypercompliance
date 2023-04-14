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

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.IfThenElse;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.DetectEnd;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Bags;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Maps;
import ca.uqac.lif.cep.util.Numbers;

/**
 * A hyperpolicy expressing that all <em>complete</em> traces in a log must
 * contain the same number of "d" events. On the simple source this property is
 * always true, since all traces have a single "d" at their end.
 * <p>
 * This property is expressed as an aggregation of the last event of each
 * trace, and a condition on the cardinality of this aggregation.
 * 
 * @author Sylvain Hallé
 */
public class SameNumberDAggregation extends GroupProcessor
{
	public static final transient String NAME = "Same d (aggregation)";
	
	public SameNumberDAggregation()
	{
		super(1, 1);
		SliceLog agg = new SliceLog(getCount(), Aggregate.Choice.INACTIVE);
		ApplyFunction condition = new ApplyFunction(new FunctionTree(Numbers.isLessOrEqual, new FunctionTree(Bags.getSize, new FunctionTree(Maps.values, StreamVariable.X)), new Constant(1)));
		connect(agg, condition);
		addProcessors(agg, condition);
		associateInput(agg);
		associateOutput(condition);
	}

	protected static Processor getCount()
	{
		GroupProcessor cnt = new GroupProcessor(1, 1);
		{
			DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant(SimpleSource.END)));
			Fork f = new Fork(3);
			connect(end, f);
			ApplyFunction eq_d = new ApplyFunction(new FunctionTree(Equals.instance, StreamVariable.X, new Constant("d")));
			connect(f, 0, eq_d, 0);
			TurnInto one = new TurnInto(1);
			connect(f, 1, one, 0);
			TurnInto zero = new TurnInto(0);
			connect(f, 2, zero, 0);
			ApplyFunction ite = new ApplyFunction(IfThenElse.instance);
			connect(eq_d, 0, ite, 0);
			connect(one, 0, ite, 1);
			connect(zero, 0, ite, 2);
			Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			connect(ite, sum);
			cnt.addProcessors(end, f, eq_d, one, zero, ite, sum);
			cnt.associateInput(end).associateOutput(sum);
		}
		return cnt;
	}
	
	@Override
	public SameNumberDAggregation duplicate(boolean with_state)
	{
		return new SameNumberDAggregation();
	}
}
