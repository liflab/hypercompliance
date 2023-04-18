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

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.GroupProcessor;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.functions.ApplyFunction;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.tmf.Fork;

/**
 * Contextualizes the evaluation of a (hyper)policy by subordinating it to the
 * truth value of another condition.
 * 
 * @author Sylvain Hallé
 */
public class Weaken extends GroupProcessor
{
  /**
   * The condition that determines if the policy should be evaluated.
   */
	protected final Processor m_phi;
	
	/**
	 * The policy to evaluate.
	 */
	protected final Processor m_psi;
	
	/**
	 * Creates a new instance of the processor.
	 * @param psi The condition that determines if the policy should be evaluated
	 * @param phi The policy to evaluate
	 */
	public Weaken(Processor psi, Processor phi)
	{
		super(1, 1);
		m_phi = phi;
		m_psi = psi;
		Fork f = new Fork();
		Connector.connect(f, 0, m_psi, 0);
		Connector.connect(f, 1, m_phi, 0);
		ApplyFunction implies = new ApplyFunction(Troolean.IMPLIES_FUNCTION);
		Connector.connect(m_psi, 0, implies, 0);
		Connector.connect(m_phi, 0, implies, 1);
		addProcessors(m_phi, m_psi, f, implies);
		associateInput(0, f, 0);
		associateOutput(0, implies, 0);
	}
	
	@Override
	public Weaken duplicate(boolean with_state)
	{
		return new Weaken(m_psi.duplicate(with_state), m_phi.duplicate(with_state));
	}
}
