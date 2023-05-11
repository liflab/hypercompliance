package hypercompliancelab.xes;

import ca.uqac.lif.cep.functions.UnaryFunction;
import ca.uqac.lif.cep.hypercompliance.LogUpdate;
import ca.uqac.lif.cep.tuples.Tuple;
import hypercompliancelab.ArrayTupleBuilder;

/**
 * In a {@link LogUpdate} event that contains a {@link Tuple}, produces
 * a new event that selects only a subset of the attributes in the tuple.
 * @author Sylvain Hallé 
 */
public class SelectColumns extends UnaryFunction<LogUpdate,LogUpdate>
{
	protected final String[] m_colNames;
	
	protected ArrayTupleBuilder m_builder;
	
	public SelectColumns(String ... col_names)
	{
		super(LogUpdate.class, LogUpdate.class);
		m_colNames = col_names;
		m_builder = new ArrayTupleBuilder(col_names);
	}

	@Override
	public LogUpdate getValue(LogUpdate u)
	{
		Tuple t = (Tuple) u.getEvent();
		Object[] vals = new Object[m_colNames.length];
		for (int i = 0; i < vals.length; i++)
		{
			vals[i] = t.get(m_colNames[i]);
		}
		Tuple new_t = m_builder.createTuple(vals);
		return new LogUpdate(u.getId(), new_t);
	}
}
