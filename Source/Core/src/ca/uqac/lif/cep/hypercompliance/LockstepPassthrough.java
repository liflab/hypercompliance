/*
    BeepBeep, an event stream processor
    Copyright (C) 2008-2023 Sylvain Hallé

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

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;

public class LockstepPassthrough extends SemiSynchronousProcessor
{
	public LockstepPassthrough(int arity)
	{
		super(arity);
	}

	@Override
	protected Pushable getNewPushableInput(int index)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Pullable getNewPullableOutput(int index)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Processor duplicate(boolean with_state)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
