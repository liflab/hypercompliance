package hypercompliancelab.xes;

import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.tmf.Passthrough;

public class LastEventOfEach extends SliceLog
{
	public LastEventOfEach()
	{
		super(new Passthrough(1), Choice.ALL);
	}
}
