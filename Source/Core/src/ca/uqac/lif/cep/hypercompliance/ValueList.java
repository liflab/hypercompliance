/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2017 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cep.hypercompliance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.cep.functions.UnaryFunction;

/**
 * Gets the list of values in a map. This is a variant on {@link Values} that
 * retains the multiplicity of each value.
 */
@SuppressWarnings("rawtypes")
public class ValueList extends UnaryFunction<Map, List>
{
	/**
	 * A single instance of the function
	 */
	public static final transient ValueList instance = new ValueList();

	protected ValueList()
	{
		super(Map.class, List.class);
	}

	@Override
	public List<?> getValue(Map x)
	{
		Collection<?> col = x.values();
		col.remove(null);
		List<Object> list = new ArrayList<Object>(col.size());
		list.addAll(col);
		return list;
	}
}