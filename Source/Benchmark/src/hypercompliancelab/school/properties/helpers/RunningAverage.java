package hypercompliancelab.school.properties.helpers;


import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Numbers;

/**
 * Calculates the running average of a stream of numerical values. This
 * (classical) function is used often enough in the examples that it deserves
 * being its own processor to avoid cluttering the code.
 *
 * @author Sylvain Hallé
 */
public class RunningAverage extends GroupProcessor
{
    /**
     * Creates a new instance of the processor.
     */
    public RunningAverage()
    {
        super(1, 1);
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
        addProcessors(f, sum_1, one, sum_2, div);
        associateInput(0, f, 0);
        associateOutput(0, div, 0);
    }

    @Override
    public RunningAverage duplicate(boolean with_state)
    {
        return new RunningAverage();
    }

    @Override
    public String toString()
    {
        return "\u0304";
    }
}
