package hypercompliancelab.simple;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.hypercompliance.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;

public class AverageLength extends Aggregate
{
	public static final String NAME = "Average length";
	
	public AverageLength()
	{
		super(getLength(), Choice.INACTIVE, getAverage());
	}
	
	protected static Processor getAverage()
	{
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
			avg.addProcessors(f, sum_1, one, sum_2, div);
			avg.associateInput(0, f, 0);
			avg.associateOutput(0, div, 0);
		}
		return avg;
	}
	
	protected static Processor getLength()
	{
		GroupProcessor length = new GroupProcessor(1, 1);
		{
			DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant(SimpleSource.END)));
			TurnInto one = new TurnInto(1);
			connect(end, one);
			Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			connect(one, sum);
			length.addProcessors(end, one, sum);
			length.associateInput(0, end, 0);
			length.associateOutput(0, sum, 0);
		}
		return length;
	}
}
