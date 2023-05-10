package hypercompliancelab.xes.bpi2011;

import ca.uqac.lif.fs.FileSystem;
import hypercompliancelab.xes.LazyInterleavedSource;

public class Bpi2011Source extends LazyInterleavedSource
{
  public static final transient String NAME = "WABO";
  
  public Bpi2011Source(FileSystem fs)
  {
    super("time:timestamp", "concept:name", fs, "lite.xes");
  }
}
