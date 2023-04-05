package examples;

import java.util.Set;

import ca.uqac.lif.cep.functions.BinaryFunction;

public class JaccardTraces
{
	@SuppressWarnings("rawtypes")
	public static class JaccardCoefficient extends BinaryFunction<Set,Set,Float>
	{
		public static final JaccardCoefficient instance = new JaccardCoefficient();
		
		protected JaccardCoefficient()
		{
			super(Set.class, Set.class, Float.class);
		}

		@Override
		public Float getValue(Set x, Set y)
		{
			
			// TODO Auto-generated method stub
			return null;
		}
	}
}
