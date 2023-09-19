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
package hypercompliancelab.school.properties;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.*;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.Describable;

import java.io.IOException;
import java.util.ArrayList;

import static ca.uqac.lif.cep.Connector.connect;

/**
 * A hyperquery that evaluates if the acceptance ratio of a process is above a given threshold.
 *
 * author: Sylvain Hallé and Chukri Soueidi
 */
public class AcceptanceRate extends Aggregate implements Describable {
    /**
     * The name of this hyperquery.
     */
    public static final transient String NAME = "Acceptance Rate";

    public static final double REJECTION_THRESHOLD = 0.4;

    public AcceptanceRate() {

        super(getProcessor(), Choice.INACTIVE, getAggregator());
    }

    protected static GroupProcessor getProcessor() {



        GroupProcessor current_a = new GroupProcessor(1, 1);
        {
            DetectEnd e = new DetectEnd(new FunctionTree(Equals.instance,
                    new FetchAttribute("state"), new Constant("END")));
            Fork f = new Fork(3);
            connect(e, f);

            ApplyFunction getState = new ApplyFunction(new FunctionTree(Equals.instance,
                    new FetchAttribute("state"), new Constant("Accepted")));

            connect(f, 0, getState, 0);
            ApplyFunction ite = new ApplyFunction(IfThenElse.instance);
            connect(getState, 0, ite, 0);
            TurnInto one = new TurnInto(1);
            connect(f, 1, one, 0);
            TurnInto zero = new TurnInto(0);
            connect(f, 2, zero, 0);
            connect(one, 0, ite, 1);
            connect(zero, 0, ite, 2);
            current_a.addProcessors(e, f, getState, one, zero, ite);
            current_a.associateInput(0, e, 0);
            current_a.associateOutput(0, ite, 0);
        }
        return current_a;
    }

    protected static GroupProcessor getAggregator() {

        GroupProcessor avg = new GroupProcessor(1, 1);
        {
            Fork f = new Fork();
            Cumulate sum_1 = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
            Connector.connect(f, 0, sum_1, 0);
            TurnInto one = new TurnInto(1);
            Connector.connect(f, 1, one, 0);
            Cumulate sum_2 = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
            Connector.connect(one, sum_2);
            ApplyFunction div = new ApplyFunction(Numbers.division);
            Connector.connect(sum_1, 0, div, 0);
            Connector.connect(sum_2, 0, div, 1);

            Fork f2 = new Fork(2);
            Connector.connect(div, f2);


            TurnInto threshold = new TurnInto(REJECTION_THRESHOLD);
            connect(f2, 0, threshold, 0);
            ApplyFunction geq = new ApplyFunction(Numbers.isGreaterOrEqual);
            connect(threshold, 0, geq, 1);
            connect(f2, 1, geq, 0);

            avg.addProcessors(f, sum_1, one, sum_2, div, f2, threshold, geq);
            avg.associateInput(0, f, 0);
            avg.associateOutput(0, geq, 0);
        }
        return avg;
    }

    public static void main(String[] args) throws FileSystemException, IOException {
        Test.runScenario(new ArrayList<String>() {
            private static final long serialVersionUID = 1L;

            {
                add("AcceptanceRate");
            }
        });
    }


    @Override
    public String getDescription() {
        return "A minimum percent of applications must reach a positive outcome";
    }
}
