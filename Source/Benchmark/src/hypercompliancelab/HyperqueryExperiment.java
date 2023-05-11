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
package hypercompliancelab;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.tmf.BlackHole;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.util.Stopwatch;
import ca.uqac.lif.units.Frequency;
import ca.uqac.lif.units.Time;
import ca.uqac.lif.units.si.Hertz;
import ca.uqac.lif.units.si.Millisecond;
import ca.uqac.lif.units.si.Second;

public class HyperqueryExperiment extends Experiment
{
	public static final transient String EVENTS = "Length";
	
	public static final transient String TIME = "Time (ms)";
	
	public static final transient String MAX_MEMORY = "Max memory (B)";
	
	public static final transient String MEMORY = "Memory (B)";
	
	public static final transient String TOTAL_EVENTS = "Events";
	
	public static final transient String TOTAL_TIME = "Total time (ms)";
	
	public static final transient String THROUGHPUT = "Throughput (Hz)";
	
	protected static int s_interval = 1000;
	
	/**
	 * The length of the source. Used by the experiment to calculate its
	 * progression.
	 */
	protected int m_sourceLength;
	
	/**
	 * The source of events.
	 */
	protected Processor m_source;
	
	/**
	 * The policy to evaluate.
	 */
	/*@ non_null @*/ protected Processor m_policy;
	
	/**
	 * The file system used to probe whether a trace file exists.
	 */
	protected LabFileSystem m_fs;
	
	HyperqueryExperiment(Processor source, Processor policy)
	{
		super();
		m_source = source;
		m_policy = policy;
		m_sourceLength = -1;
		describe(EVENTS, "The number of input events processed by the policy");
		describe(TIME, "The progressive time taken to evaluate the policy on the log");
		describe(MEMORY, "The progressive memory consumed to evaluate the policy on the log");
		describe(TOTAL_TIME, "The total time taken to evaluate the policy on the log", Time.DIMENSION);
		describe(TOTAL_EVENTS, "The total number of events in the input trace");
		describe(THROUGHPUT, "The number of events per second processed by the hyperpolicy", Frequency.DIMENSION);
		setTimeout(new Second(30));
	}
	
	HyperqueryExperiment()
	{
		this(null, null);
	}
	
	public void setSourceLength(int length)
	{
		m_sourceLength = length;
	}
	
	@Override
	public void execute() throws ExperimentException
	{
		Connector.connect(m_policy, new BlackHole());
		Pullable pl = m_source.getPullableOutput();
		Pushable ph = m_policy.getPushableInput();
		JsonList l_events = new JsonList();
		JsonList l_time = new JsonList();
		JsonList l_mem = new JsonList();
		l_events.add(0l);
		l_time.add(0l);
		writeOutput(EVENTS, l_events);
		writeOutput(TIME, l_time);
		writeOutput(MEMORY, l_mem);
		long ev_cnt = 0, max_mem = 0;
		ProcessorSizePrinter printer = new ProcessorSizePrinter();
		printer.ignoreAccessChecks(true);
		long mem = getMemory(printer);
		l_mem.add(mem);
		max_mem = Math.max(mem, max_mem);
		// Populate source
		m_source.start();
		// Get source length
		int source_length = 0;
		if (m_source instanceof BoundedSource)
		{
			source_length = ((BoundedSource) m_source).eventCount();
		}
		// Process entire source
		Stopwatch.start(this);
		while (pl.hasNext())
		{
			ev_cnt++;
			Object o = pl.pull();
			ph.push(o);
			if (ev_cnt % s_interval == 0)
			{
				l_events.add(ev_cnt);
				l_time.add(Stopwatch.lap(this));
				l_mem.add(getMemory(printer));
				if (source_length > 0)
				{
					setProgression((float) ev_cnt / (float) source_length);
				}
			}
		}
		long duration = Stopwatch.stop(this);
		writeOutput(TOTAL_TIME, new Millisecond(duration));
		writeOutput(TOTAL_EVENTS, ev_cnt);
		writeOutput(MAX_MEMORY, max_mem);
		writeOutput(THROUGHPUT, new Hertz(ev_cnt / duration * 1000));
	}
	
	@Override
	public boolean prerequisitesFulfilled()
	{
		if (m_source instanceof LocalFileSource)
		{
			try
			{
				return ((LocalFileSource) m_source).prerequisitesFulfilled();
			}
			catch (FileSystemException e)
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void fulfillPrerequisites()
	{
		if (m_source instanceof LocalFileSource)
		{
			try
			{
				((LocalFileSource) m_source).fulfillPrerequisites();
			}
			catch (FileSystemException e)
			{
				return;
			}
		}
	}
	
	protected long getMemory(ProcessorSizePrinter printer) throws ExperimentException
	{
		try
		{
			printer.reset();
			return printer.print(m_policy).longValue();
		}
		catch (PrintException e)
		{
			throw new ExperimentException(e);
		}
	}
}
