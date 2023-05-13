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

import java.io.IOException;
import java.io.InputStream;

import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.functions.Function;
import ca.uqac.lif.cep.hypercompliance.InterleavedSource;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.XesToLog;
import ca.uqac.lif.fs.FileSystemException;
import hypercompliancelab.LabFileSystem;
import hypercompliancelab.LocalFileSource;

/**
 * An {@link InterleavedSource} that only loads its events on a call to
 * {@link #start()}. This behavior allows these sources to be rapidly
 * instantiated when the lab is started, and to only read the contents of the
 * XES file they feed from (a potentially long operation) when an experiment
 * that uses them is run.
 * 
 * @author Sylvain Hallé
 *
 */
public abstract class LazyInterleavedSource extends InterleavedSource implements LocalFileSource
{
	/**
	 * The file system where the XES files are downloaded to.
	 */
  protected final LabFileSystem m_fs;
  
  /**
   * The name of the local (XES) file this source will read
   * its log from.
   */
  protected final String m_filename;
  
  /**
   * The name of the attribute in the XES file that corresponds to the case
   * identifier.
   */
  protected final String m_caseId;
  
  /**
   * Creates a new instance of the source.
   * @param timestamp The name of the attribute in the XES file containing
   * the timestamp of each event
   * @param case_id The name of the attribute in the XES file that corresponds
   * to the case identifier 
   * @param fs The file system where the XES files are downloaded to
   * @param filename The name of the local (XES) file this source will read
   * its log from 
   */
  public LazyInterleavedSource(String timestamp, String case_id, LabFileSystem fs, String filename)
  {
    super(timestamp);
    m_caseId = case_id;
    m_fs = fs;
    m_filename = filename;
  }
  
  /**
   * Prepares the source to provide output events. In the case of this class,
   * this method pulls the log from the local XES file, turns it into a
   * {@link Log} object, and then populates its list of {@link LogUpdate}
   * events by interleaving the various traces according to the timestamp of
   * each event.
   */
  @Override
  public void start() throws ProcessorException
  {
    super.start();
    try
    {
      InputStream is = m_fs.readFrom(m_filename);
      XesToLog x = new XesToLog(m_caseId);
      Log log = x.getLog(is);
      is.close();
      populateFromLog(log);
    }
    catch (FileSystemException e)
    {
      throw new ProcessorException(e);
    }
    catch (IOException e)
    {
      throw new ProcessorException(e);
    }
  }
  
  @Override
  public int eventCount()
  {
  	return m_events.size();
  }
  
  @Override
  public boolean prerequisitesFulfilled() throws FileSystemException
  {
  	return m_fs.isFile(m_filename);
  }
  
  @Override
	public void clean() throws FileSystemException
	{
		m_fs.delete(m_filename);
	}
  
  public String getFilename()
  {
  	return m_filename;
  }
  
  /**
   * Gets the function that determines if a {@link LogUpdate} event
   * corresponds to the end of the trace it belongs to. Each concrete
   * descendant of this class must define its own condition according to the
   * contents of its log.
   * @return The function
   */
  /*@ non_null @*/ public abstract Function getEndCondition();
  
  /**
   * Gets the function that extracts the "action" of each event.
   * @return The function
   */
  /*@ non_null @*/ public abstract Function getAction();
  
  /**
   * Gets the function that extracts the "ttimestamp" of each event.
   * @return The function
   */
  /*@ non_null @*/ public abstract Function getTimestamp();
}
