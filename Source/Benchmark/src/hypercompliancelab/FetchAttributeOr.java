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

import ca.uqac.lif.cep.tuples.FetchAttribute;
import ca.uqac.lif.cep.tuples.Tuple;

/**
 * A variant of {@link FetchAttribute} that produces a non-null value in case
 * the attribute is not defined in a tuple. This function is used since some
 * real-world XES logs do not contain the "action" attribute in all their
 * events.
 * @author Sylvain Hallé
 *
 */
public class FetchAttributeOr extends FetchAttribute
{
	/**
	 * The alternative value to return if the attribute is not defined in the
	 * tuple.
	 */
	protected final Object m_orValue;
	
	/**
	 * Creates a new instance of the function.
	 * @param attribute_name The name of the attribute to fetch in the tuple
	 * @param or_value The alternative value to return if the attribute is not
	 * defined in the tuple.
	 */
	public FetchAttributeOr(String attribute_name, Object or_value)
	{
		super(attribute_name);
		m_orValue = or_value;
	}
	
	@Override
	public Object getValue(Tuple t)
	{
		Object o = super.getValue(t);
		return o == null ? m_orValue : o;
	}
	
	@Override
	public FetchAttributeOr duplicate(boolean with_state)
	{
		return this;
	}
}
