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

import ca.uqac.lif.cep.Processor;

/**
 * An object that can provide a pipeline expressing a hyperquery when
 * requested.
 * @author Sylvain Hallé
 */
public interface HyperqueryProvider
{
	public static final String QUERY = "Hyperquery";
	
	/**
	 * Gets a new instance of a hyperpolicy.
	 * @return The pipeline
	 */
	public Processor getProcessor();
}
