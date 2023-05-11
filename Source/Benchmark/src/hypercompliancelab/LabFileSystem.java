package hypercompliancelab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.FileUtils;
import ca.uqac.lif.fs.HardDisk;

public class LabFileSystem extends HardDisk
{
	public LabFileSystem(String path)
	{
		super(path);
	}
	
	/**
	 * Downloads a file and saves it locally in the "data" folder.
	 * @param url The URL to download the file from
	 * @param local_filename The name of the file when saved locally
	 * @throws FileSystemException Thrown if the operation does not succeed 
	 */
	public void download(String url, String local_filename) throws FileSystemException
	{
		URL u;
		try
		{
			u = new URL(url);
			URLConnection c = u.openConnection();
			InputStream is = c.getInputStream();
			OutputStream os = writeTo(local_filename);
			FileUtils.copy(is, os);
			is.close();
		}
		catch (MalformedURLException e)
		{
			throw new FileSystemException(e);
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	/**
	 * Extracts the contents of a local Gzip file into the current folder.
	 * @param from The name of the local Gzip file
	 * @param to The name that will be given to the extracted file
	 * @throws FileSystemException Thrown if the operation does not succeed
	 */
	public void gunzip(String from, String to) throws FileSystemException
	{
		InputStream is = readFrom(from);
		try
		{
			GZIPInputStream gis = new GZIPInputStream(is);
			OutputStream os = writeTo(to);
			FileUtils.copy(gis, os);
			os.close();
			gis.close();
			is.close();
		}
		catch (IOException e)
		{
			throw new FileSystemException(e);
		}
	}
	
	@Override
	public LabFileSystem open() throws FileSystemException
	{
		super.open();
		return this;
	}
}
