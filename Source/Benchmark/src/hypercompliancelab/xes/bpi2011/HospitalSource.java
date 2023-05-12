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

/**
 * A source of {@link LogUpdate} events that takes its data from the
 * <i>Hospital</i> data set (DOI:
 * {@code 10.4121/uuid:d9769f3d-0ab0-4fb8-803b-0d1120ffcf54}).
 * @author Sylvain Hallé
 *
 */
public class HospitalSource extends LazyInterleavedSource
{
	/**
	 * The name of this source, which also gives the name to the corresponding
	 * scenario.
	 */
  /*@ non_null @*/ public static final transient String NAME = "Hospital";
  
  /**
   * The URL pointing to the zipped XES file.
   */
  protected static final transient String s_xesUrl = "https://data.4tu.nl/file/5ea5bb88-feaa-4e6f-a743-6460a755e05b/6f9640f9-0f1e-44d2-9495-ef9d1bd82218";
  
  /**
   * The name of the XES file.
   */
  protected static final transient String s_xesFilename = "Hospital_log.xes";
  
  /**
   * The name of the zipped XES file.
   */
  protected static final transient String s_gzFilename = "Hospital_log.xes.gz";
  
  /**
   * Creates a new instance of the source.
   * @param fs The file system where the XES files are downloaded to.
   */
  public HospitalSource(LabFileSystem fs)
  {
    super("time:timestamp", "concept:name", fs, s_xesFilename);
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
		m_fs.download(s_xesUrl, s_gzFilename);
		m_fs.gunzip(s_gzFilename, s_xesFilename);
		m_fs.delete(s_gzFilename);
	}
	
	@Override
	public void clean() throws FileSystemException
	{
		if (m_fs.isFile(s_gzFilename))
		{
			m_fs.delete(s_gzFilename);
		}
		if (m_fs.isFile(s_xesFilename))
		{
			m_fs.delete(s_xesFilename);
		}
	}
}
