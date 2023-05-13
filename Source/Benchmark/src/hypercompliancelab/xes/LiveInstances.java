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
package hypercompliancelab.xes;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.util.Numbers;
import hypercompliancelab.Describable;

/**
 * Counts the number of process instances that are ongoing concurrently at any
 * time point. 
 * @author Sylvain Hallé
 */
public class LiveInstances extends Aggregate implements Describable
{
	/**
	 * The name of this hyperquery.
	 */
	public static final transient String NAME = "Concurrent instances";
	
  public LiveInstances(Function end_condition)
  {
    super(getPerInstance(end_condition), Choice.ACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), new Object[] {0});
  }
  
  protected static GroupProcessor getPerInstance(Function end_condition)
  {
    return new GroupProcessor(1, 1) {{
      DetectEnd end = new DetectEnd(end_condition);
      TurnInto one = new TurnInto(1);
      Connector.connect(end, one);
      addProcessors(end, one).associateInput(end).associateOutput(one);
    }};
  }

	@Override
	public String getDescription()
	{
		return "Counts the number of process instances that are ongoing concurrently at any time point";
	}
}
