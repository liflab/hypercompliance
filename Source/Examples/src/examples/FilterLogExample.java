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

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.IfThenElse;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.FilterLogs;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.io.Print;
import ca.uqac.lif.cep.ltl.SoftCast;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;

import static ca.uqac.lif.cep.Connector.connect;

/**
 * Example of a log filtering task based on a condition on individual traces.
 * In this example, log updates are let through only for those traces contain
 * a minimum number <i>n</i> of "a" events.
 */
public class FilterLogExample
{
  public static void main(String[] args)
  {
  	/* The number of "a" events required for events of a trace to be let
  	 * through. */
    int n = 2;
    
    /* Creates the processor pipeline that checks if a trace has
     * at least n "a" events. */
    GroupProcessor hasNA = new GroupProcessor(1, 1);
    {
      Fork f1 = new Fork();
      ApplyFunction eq1 = new ApplyFunction(Equals.instance);
      connect(f1, 0, eq1, 0);
      TurnInto a = new TurnInto("a");
      connect(f1, 1, a, 0);
      connect(a, 0, eq1, 1);
      Fork f2 = new Fork(3);
      connect(eq1, f2);
      ApplyFunction ite = new ApplyFunction(IfThenElse.instance);
      connect(f2, 0, ite, 0);
      TurnInto one = new TurnInto(1);
      connect(f2, 1, one, 0);
      connect(one, 0, ite, 1);
      TurnInto zero = new TurnInto(0);
      connect(f2, 2, zero, 0);
      connect(zero, 0, ite, 2);
      Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
      connect(ite, sum);
      Fork f3 = new Fork();
      connect(sum, f3);
      ApplyFunction gt = new ApplyFunction(new FunctionTree(SoftCast.instance, Numbers.isGreaterThan));
      connect(f3, 0, gt, 0);
      TurnInto enn = new TurnInto(n);
      connect(f3, 1, enn, 0);
      connect(enn, 0, gt, 1);
      hasNA.associateInput(0, f1, 0);
      hasNA.associateOutput(0, gt, 0);
      hasNA.addProcessors(f1, eq1, a, f2, one, zero, ite, sum, f3, gt, enn);
    }
    FilterLogs filter = new FilterLogs(hasNA);
    Print print = new Print();
    connect(filter, print);
    Pushable p = filter.getPushableInput();
    p.push(new LogUpdate(0, "a"));
    p.push(new LogUpdate(0, "b"));
    p.push(new LogUpdate(1, "c"));
    p.push(new LogUpdate(0, "a"));
    p.push(new LogUpdate(0, "a"));
    p.push(new LogUpdate(1, "a"));
    p.push(new LogUpdate(1, "a"));
    p.push(new LogUpdate(0, "c"));
    p.push(new LogUpdate(1, "a"));
    
  }
}
