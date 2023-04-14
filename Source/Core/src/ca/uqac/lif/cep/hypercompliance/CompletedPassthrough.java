package ca.uqac.lif.cep.hypercompliance;

import java.util.Queue;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.ProcessorException;
import ca.uqac.lif.cep.SynchronousProcessor;

public class CompletedPassthrough extends SynchronousProcessor
{
	protected boolean m_seenEndOfTrace;
	
	public CompletedPassthrough(int arity)
	{
		super(arity, arity);
		m_seenEndOfTrace = false;
	}
	
	@Override
  protected boolean onEndOfTrace(Queue<Object[]> outputs) throws ProcessorException
  {
  	super.onEndOfTrace(outputs);
  	m_seenEndOfTrace = true;
  	return false;
  }
	
	@Override
	public void reset()
	{
		super.reset();
		m_seenEndOfTrace = false;
	}
	
	/**
   * Queries if the processor has seen the end of the input trace.
   * @return {@code true} if the end of trace signal has been received,
   * {@code false} otherwise
   */
  /*@ pure @*/ public boolean seenEndOfTrace()
  {
  	return m_seenEndOfTrace;
  }

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		Object[] out = new Object[inputs.length];
		for (int i = 0; i < inputs.length; i++)
		{
			out[i] = inputs[i];
		}
		outputs.add(out);
		return true;
	}

	@Override
	public Processor duplicate(boolean with_state)
	{
		CompletedPassthrough cp = new CompletedPassthrough(getInputArity());
		if (with_state)
		{
			cp.m_seenEndOfTrace = m_seenEndOfTrace;
		}
		return cp;
	}
}
