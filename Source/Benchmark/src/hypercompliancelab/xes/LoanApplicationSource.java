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
package hypercompliancelab.xes;

import java.util.Arrays;

import ca.uqac.lif.cep.functions.Constant;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.functions.FunctionTree;
import ca.uqac.lif.cep.functions.StreamVariable;
import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.Tuple;
import ca.uqac.lif.cep.tuples.TupleMap;
import ca.uqac.lif.cep.util.Equals;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.Describable;
import hypercompliancelab.LabFileSystem;

/**
 * A source of {@link LogUpdate} events that takes its data from the
 * <i>Loan application</i> data set (DOI:
 * {@code 10.4121/uuid:3926db30-f712-4394-aebc-75976070e91f}), which was part
 * of the BPI Challenge 2012.
 * <p>
 * Contrary to other sources in this package, the XES log has separate events
 * for the start and the end of an activity. Thus, the "label" of an event
 * is a tuple made of an activity and a lifecycle step (e.g. start, end, etc.). 
 * 
 * @author Sylvain Hallé
 */
public class LoanApplicationSource extends LazyInterleavedSource implements Describable
{
	/**
	 * The name of this source, which also gives the name to the corresponding
	 * scenario.
	 */
	/*@ non_null @*/ public static final transient String NAME = "Loan application";

	/**
	 * The URL pointing to the zipped XES file.
	 */
	protected static final transient String s_xesUrl = "https://data.4tu.nl/file/533f66a4-8911-4ac7-8612-1235d65d1f37/3276db7f-8bee-4f2b-88ee-92dbffb5a893";

	/**
	 * The name of the XES file.
	 */
	protected static final transient String s_xesFilename = "BPI_Challenge_2012.xes";

	/**
	 * The name of the zipped XES file.
	 */
	protected static final transient String s_gzFilename = "BPI_Challenge_2012.xes.gz";

	/**
	 * Creates a new instance of the source.
	 * @param fs The file system where the XES files are downloaded to.
	 */
	public LoanApplicationSource(LabFileSystem fs)
	{
		super("time:timestamp", "concept:name", fs, s_xesFilename);
	}

	@Override
	protected void populateFromLog(Log log)
	{
		// Add the END event at the end of each trace in the log
		Tuple end_event = new TupleMap();
		end_event.put("concept:name", "END");
		end_event.put("lifecycle:transition", "COMPLETE");
		end_event.put("time:timestamp", Long.MAX_VALUE);
		log.appendToAll(Arrays.asList(end_event));
		super.populateFromLog(log);
	}

	@Override
	public Function getEndCondition()
	{
		return new FunctionTree(Equals.instance, new Constant("END"), new FunctionTree(new FetchAttribute("concept:name"), StreamVariable.X));
	}

	@Override
	public Function getAction()
	{
		return new ToEventLabel();
	}
	
	@Override
	public Function getTimestamp()
	{
		return new FetchAttribute("time:timestamp");
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
	
	protected static class ToEventLabel extends UnaryFunction<Tuple,EventLabel>
	{
		public ToEventLabel()
		{
			super(Tuple.class, EventLabel.class);
		}

		@Override
		public EventLabel getValue(Tuple t)
		{
			return new EventLabel((String) t.get("concept:name"), (String) t.get("lifecycle:transition"));
		}
	}
	
	protected static class EventLabel
	{
		private final String m_name;
		
		private final String m_step;
		
		public EventLabel(String name, String step)
		{
			super();
			m_name = name;
			m_step = step;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof EventLabel))
			{
				return false;
			}
			EventLabel el = (EventLabel) o;
			return m_name.compareTo(el.m_name) == 0 && m_step.compareTo(el.m_step) == 0;
		}
		
		@Override
		public int hashCode()
		{
			return m_name.hashCode();
		}
	}
	
	@Override
	public String getDescription()
	{
		return "A log of cases from a loan application process";
	}
}
