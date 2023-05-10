package hypercompliancelab.xes;

import java.io.IOException;
import java.io.InputStream;

import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.hypercompliance.InterleavedSource;
import ca.uqac.lif.cep.hypercompliance.Log;
import ca.uqac.lif.cep.hypercompliance.XesToLog;
import ca.uqac.lif.fs.FileSystem;
import ca.uqac.lif.fs.FileSystemException;

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
public class LazyInterleavedSource extends InterleavedSource
{
  protected final FileSystem m_fs;
  
  protected final String m_filename;
  
  protected final String m_caseId;
  
  public LazyInterleavedSource(String timestamp, String case_id, FileSystem fs, String filename)
  {
    super(timestamp);
    m_caseId = case_id;
    m_fs = fs;
    m_filename = filename;
  }
  
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
}