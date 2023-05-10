package hypercompliancelab.xes.bpi2011;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.hypercompliance.InterleavedSource;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.SliceLog;
import ca.uqac.lif.cep.hypercompliance.SliceLog.Choice;
import ca.uqac.lif.cep.hypercompliance.XesToLog;
import ca.uqac.lif.cep.tmf.DetectEnd;
import ca.uqac.lif.cep.tmf.KeepLast;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.Tuple;
import ca.uqac.lif.cep.tuples.TupleMap;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.HardDisk;
import hypercompliancelab.xes.AverageLength;
import hypercompliancelab.xes.SelectColumns;

public class Test
{

	public static void main(String[] args) throws FileSystemException, IOException
	{
		Tuple end_event = new TupleMap();
		end_event.put("action_code", "END");
		end_event.put("time:timestamp", Long.MAX_VALUE);
		HardDisk disk = new HardDisk("/tmp").open();
		InputStream is = disk.readFrom("CoSeLoG WABO 1.xes");
		XesToLog x = new XesToLog("concept:name");
		Log log = x.getLog(is);
		is.close();
		disk.close();
		log.appendToAll(Arrays.asList(end_event));
		System.out.println(log.keySet().size());
		InterleavedSource source = new InterleavedSource(log, "time:timestamp");
		ApplyFunction project = new ApplyFunction(new SelectColumns(new String[] {"action_code"}));
		Connector.connect(source, project);
		//SliceLog slice = new SliceLog(new DetectEnd(new FunctionTree(Equals.instance, new Constant("END"), new FunctionTree(new FetchAttribute("action_code"), StreamVariable.X))), Choice.ACTIVE);
		AverageLength avg = new AverageLength(new FunctionTree(Equals.instance, new Constant("END"), new FunctionTree(new FetchAttribute("action_code"), StreamVariable.X)));
		Connector.connect(project, avg);
		KeepLast last = new KeepLast();
		Connector.connect(avg, last);
		Pullable p = last.getPullableOutput();
		System.out.println(p.pull());
	}

}
