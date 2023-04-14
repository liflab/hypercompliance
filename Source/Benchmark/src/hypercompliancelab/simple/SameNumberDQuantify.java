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
import ca.uqac.lif.cep.hypercompliance.DetectEnd;
import ca.uqac.lif.cep.hypercompliance.Quantify;
import ca.uqac.lif.cep.ltl.SoftCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;

/**
 * A hyperpolicy expressing that all <em>complete</em> traces in a log must
 * contain the same number of "d" events. On the simple source this property is
 * always true, since all traces have a single "d" at their end.
 * <p>
 * This property is expressed as a quantification over pairs of complete
 * traces. The condition stipulates that they must have the same number of
 * "d" events.
 * 
 * @author Sylvain Hallé
 */
public class SameNumberDQuantify extends Quantify
{
	public static final transient String NAME = "Same d (quantification)";
	
	public SameNumberDQuantify()
	{
		super(getCondition(), true, true, QuantifierType.ALL, QuantifierType.ALL);
	}
	
	protected static Processor getCondition()
	{
		GroupProcessor gp = new GroupProcessor(2, 1);
		{
			Processor p1 = getCount();
			Processor p2 = getCount();
			ApplyFunction eq = new ApplyFunction(new FunctionTree(SoftCast.instance, new FunctionTree(Equals.instance, StreamVariable.X, StreamVariable.Y)));
			connect(p1, 0, eq, 0);
			connect(p2, 0, eq, 1);
			gp.addProcessors(p1, p2, eq);
			gp.associateInput(0, p1, 0);
			gp.associateInput(1, p2, 0);
			gp.associateOutput(0, eq, 0);
		}
		return gp;
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
	public SameNumberDQuantify duplicate(boolean with_state)
	{
		return new SameNumberDQuantify();
	}
}
