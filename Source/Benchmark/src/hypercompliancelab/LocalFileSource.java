package hypercompliancelab;

import ca.uqac.lif.fs.FileSystemException;

public interface LocalFileSource
{
	public boolean prerequisitesFulfilled() throws FileSystemException;
	
	public void fulfillPrerequisites() throws FileSystemException;
	
	public void clean() throws FileSystemException;
}
