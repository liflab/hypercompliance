package hypercompliancelab;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.tmf.BlackHole;
import hypercompliancelab.simple.AverageLength;

public class Test
{

	public static void main(String[] args) throws PrintException
	{
		AverageLength a = new AverageLength();
		BlackHole h = new BlackHole();
		Connector.connect(a, h);
		Pushable p = a.getPushableInput();
		p.push(new LogUpdate(0, "a"));
		p.push(new LogUpdate(0, "END"));
		p.push(new LogUpdate(0, "END"));
	}

}
