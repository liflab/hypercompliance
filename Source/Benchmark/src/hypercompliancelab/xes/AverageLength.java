package hypercompliancelab.xes;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.Fork;
import ca.uqac.lif.cep.util.Numbers;

public class AverageLength extends Aggregate
{
	public AverageLength(Function end_condition)
	{
		super(getPerSlice(end_condition), Choice.ALL, getAggregation());
	}
	
	protected static final GroupProcessor getPerSlice(Function end_condition)
	{
		return new GroupProcessor(1, 1) {{
			DetectEnd end = new DetectEnd(end_condition);
			TurnInto one = new TurnInto(1);
			connect(end, one);
			Cumulate sum = new Cumulate(new CumulativeFunction<Number>(Numbers.addition));
			connect(one, sum);
			addProcessors(end, one, sum);
			associateInput(0, one, 0);
			associateOutput(0, sum, 0);
		}};
	}
	
	protected static final GroupProcessor getAggregation()
	{
		return new GroupProcessor(1, 1) {{
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
		}};
	}
}
