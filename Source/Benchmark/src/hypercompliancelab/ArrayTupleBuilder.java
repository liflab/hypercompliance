package hypercompliancelab;

import ca.uqac.lif.cep.tuples.TupleFixed;

/**
 * Utility class to create instances of tuples with a fixed
 * schema
 * 
 * @author Sylvain Hallé
 */
public final class ArrayTupleBuilder
{
	private final String[] m_names;
	
	public ArrayTupleBuilder(String ... names)
	{
		super();
		m_names = names;
	}
	
	public final TupleFixed createTuple(Object[] values)
	{
		return new TupleFixed(m_names, values);
	}
	
	public final TupleFixed createTupleFromString(String[] values)
	{
		Object[] eml_values = new Object[values.length];
		for (int i = 0; i < values.length; i++)
		{
			eml_values[i] = values[i];
		}
		return new TupleFixed(m_names, eml_values);
	}
}