package hypercompliancelab.simple;

import static ca.uqac.lif.cep.Connector.connect;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Cumulate;
import ca.uqac.lif.cep.functions.CumulativeFunction;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.TurnInto;
import ca.uqac.lif.cep.hypercompliance.Aggregate;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.cep.util.Numbers;

public class NumberRunning extends Aggregate
{
	public static final String NAME = "Live instances";
	
	public NumberRunning()
	{
		super(getOne(), Choice.ACTIVE, new Cumulate(new CumulativeFunction<Number>(Numbers.addition)), new Object[] {0});
	}
	
	protected static Processor getOne()
	{
		GroupProcessor gp = new GroupProcessor(1, 1);
		{
			DetectEnd end = new DetectEnd(new FunctionTree(Equals.instance, StreamVariable.X, new Constant(SimpleSource.END)));
			TurnInto one = new TurnInto(1);
			connect(end, one);
			gp.addProcessors(end, one).associateInput(end).associateOutput(one);
		}
		return gp;
	}
}
