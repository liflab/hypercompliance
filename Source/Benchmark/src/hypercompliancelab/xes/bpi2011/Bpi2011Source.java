package hypercompliancelab.xes.bpi2011;

import java.util.Arrays;

import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.Tuple;
import ca.uqac.lif.cep.tuples.TupleMap;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.LabFileSystem;
import hypercompliancelab.xes.LazyInterleavedSource;

public class Bpi2011Source extends LazyInterleavedSource
{
  public static final transient String NAME = "WABO";
  
  public Bpi2011Source(LabFileSystem fs)
  {
    super("time:timestamp", "concept:name", fs, "CoSeLoG WABO 1.xes");
  }
  
  @Override
  protected void populateFromLog(Log log)
  {
  	// Add the END event at the end of each trace in the log
  	Tuple end_event = new TupleMap();
		end_event.put("action_code", "END");
		end_event.put("time:timestamp", Long.MAX_VALUE);
		log.appendToAll(Arrays.asList(end_event));
		super.populateFromLog(log);
  }

	@Override
	public Function getEndCondition()
	{
		return new FunctionTree(Equals.instance, new Constant("END"), new FunctionTree(new FetchAttribute("action_code"), StreamVariable.X));
	}

	@Override
	public void fulfillPrerequisites() throws FileSystemException
	{
		// Download and unzip the XES file retrieved from the repository
		m_fs.download("https://data.4tu.nl/file/a928c371-d8e8-4c14-95db-2c28078085b5/117823ec-6f79-4362-8ca6-8a190c8421a2", "CoSeLoG WABO 1.xes.gz");
		m_fs.gunzip("CoSeLoG WABO 1.xes.gz", "CoSeLoG WABO 1.xes");
	}	
}
