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
package ca.uqac.lif.cep.hypercompliance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.lif.cep.ltl.Troolean;
import ca.uqac.lif.cep.ltl.Troolean.Value;
import ca.uqac.lif.cep.tmf.QueueSink;

public class Quantify extends SynchronousProcessor
{
	/*@ non_null @*/ protected final QuantifierType[] m_quantifiers;

	/*@ non_null @*/ protected final List<LogUpdate> m_inputLog;

	/**
	 * The processor to evaluate on n-uplets of traces.
	 */
	/*@ non_null @*/ protected final Processor m_phi;

	/**
	 * The root node of the quantifier node structure.
	 */
	/*@ non_null @*/ protected final QuantifierNode m_root;

	/**
	 * A flag indicating if quantifiers should be evaluated only on completed
	 * traces.
	 */
	protected final boolean m_onlyCompleted;

	/**
	 * A flag indicating if successive quantifiers apply to distinct traces.
	 */
	protected final boolean m_distinct;

	/**
	 * An enumerated type used to designate each quantifier in an expression.
	 * Possible values are:
	 * <ul>
	 * <li>{@code ALL}: universal quantification over traces in the log</li>
	 * <li>{@code SOME}: existential quantification over traces in the log</li>
	 * </ul>
	 */
	/*@ non_null @*/ public static enum QuantifierType {ALL, SOME}

	public Quantify(/*@ non_null @*/ Processor phi, boolean completed, boolean distinct, /*@ non_null @*/ QuantifierType ... quantifiers)
	{
		super(1, 1);
		m_quantifiers = quantifiers;
		m_phi = phi;
		m_inputLog = new ArrayList<LogUpdate>();
		m_onlyCompleted = completed;
		m_distinct = distinct;
		if (m_quantifiers[0] == QuantifierType.ALL)
		{
			m_root = new UniversalNode(0);
		}
		else
		{
			m_root = new ExistentialNode(0);
		}
	}

	@Override
	protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
	{
		LogUpdate upd = (LogUpdate) inputs[0];
		m_root.push(upd);
		Troolean.Value verdict = m_root.getVerdict();
		if (verdict != null)
		{
			outputs.add(new Object[] {verdict});
		}
		m_inputLog.add(upd);
		return true;
	}

	@Override
	public Processor duplicate(boolean with_state)
	{
		throw new UnsupportedOperationException("This processor cannot be duplicated");
	}

	protected abstract class QuantifierNode
	{
		/**
		 * The list of children of this node.
		 */
		/*@ non_null @*/ protected final List<QuantifierEdge> m_children;

		/**
		 * The edge from the parent of this node to this node.
		 */
		/*@ non_null @*/ protected QuantifierEdge m_parentEdge;

		/**
		 * The nesting level of this quantifier.
		 */
		protected final int m_level;

		/**
		 * The set of trace identifiers seen so far.
		 */
		/*@ non_null @*/ protected final Set<Object> m_seenIdentifiers;

		protected final Set<Object> m_parentIdentifiers;

		/**
		 * Creates a new quantifier node.
		 * @param level The nesting level of this quantifier
		 */
		QuantifierNode(int level)
		{
			super();
			m_children = new ArrayList<QuantifierEdge>();
			m_level = level;
			m_seenIdentifiers = new HashSet<Object>();
			m_parentEdge = null;
			m_parentIdentifiers = new HashSet<Object>();
		}

		/**
		 * Sets the edge that goes from the parent of this node to this node.
		 * @param e The edge
		 */
		public void setParentEdge(QuantifierEdge e)
		{
			m_parentEdge = e;
		}

		/**
		 * Gets the edge that goes from the parent of this node to this node.
		 * @return The edge
		 */
		/*@ null @*/ public QuantifierEdge getParentEdge()
		{
			return m_parentEdge;
		}

		/**
		 * Determines if the current node has an edge labelled with a given trace
		 * identifier.
		 * @param id The trace identifier
		 * @return {@code true} if the node has such an edge, {@code false}
		 * otherwise
		 */
		/*@ pure @*/ protected boolean hasEdgeFor(/*@ null @*/ Object id)
		{
			for (QuantifierEdge e : m_children)
			{
				if (e.isFor(id))
				{
					return true;
				}
			}
			return false;
		}

		/**
		 * Pushes a new event into the quantifier structure.
		 * @param u The log update event
		 */
		public final void push(LogUpdate u)
		{
			push(u, new HashSet<Integer>());
		}

		protected void push(LogUpdate u, Set<Integer> positions)
		{
			Object trace_id = u.getId();
			if (!m_seenIdentifiers.contains(trace_id))
			{
				m_seenIdentifiers.add(trace_id);
				// First spawn a copy of the sub-tree and append it as a new child
				QuantifierNode new_child = copy(m_level + 1, trace_id, this.m_parentIdentifiers);
				if (new_child != null)
				{
					m_children.add(new QuantifierEdge(this, trace_id, new_child));
					// Push all history into this new sub-tree
					for (LogUpdate e : m_inputLog)
					{
						Set<Integer> parent_pos = new HashSet<Integer>();
						getParentPositions(e.getId(), parent_pos);
						new_child.push(e, parent_pos);
					}
				}
			}
			// Then push new event into all children
			for (QuantifierEdge e : m_children)
			{
				if (e.isFor(trace_id))
				{
					Set<Integer> new_positions = new HashSet<Integer>();
					new_positions.addAll(positions);
					new_positions.add(m_level);
					e.m_destination.push(u, new_positions);
				}
				else
				{
					// Don't bother creating a new set if we don't add anything
					e.m_destination.push(u, positions);
				}
			}
		}

		/**
		 * Gets the levels in the tree structure where a given trace identifier
		 * is associated to a parent of this node.
		 * @param id The trace identifier
		 * @param parent_pos A set where the level indices are added
		 */
		protected void getParentPositions(Object id, Set<Integer> parent_pos)
		{
			QuantifierEdge e = getParentEdge();
			if (e != null)
			{
				if (e.getLabel().equals(id))
				{
					parent_pos.add(m_level - 1);
				}
				e.getSource().getParentPositions(id, parent_pos);
			}
		}

		/**
		 * Determines if a trace identifier is distinct for trace identifers
		 * of all edges leading to the root of the tree from the current node.
		 * @param id The identifier
		 * @return {@code true} if the identifier is distinct, {@code false}
		 * otherwise
		 */
		protected boolean isDistinct(Object id)
		{
			return !m_parentIdentifiers.contains(id);
		}

		@Override
		public String toString()
		{
			StringBuilder out = new StringBuilder();
			toString(out, "", "");
			return out.toString();
		}

		protected void toString(StringBuilder out, String edge, String indent)
		{
			out.append(indent).append(edge).append(" ").append(getSymbol()).append("\n");
			String new_indent = indent + " ";
			for (QuantifierEdge e : m_children)
			{
				e.getDestination().toString(out, e.toString(), new_indent);
			}
		}

		/*@ non_null @*/ protected abstract String getSymbol();

		/**
		 * Gets the current verdict of this quantifier node.
		 * @return The verdict
		 */
		/*@ non_null @*/ public abstract Troolean.Value getVerdict();

		protected QuantifierNode copy(int level, Object new_trace_id, Set<Object> parent_ids)
		{
			if (m_distinct && parent_ids.contains(new_trace_id))
			{
				return null;
			}
			if (level == m_quantifiers.length)
			{
				return new LeafNode(level + 1);
			}
			QuantifierType qt = m_quantifiers[level];
			QuantifierNode new_child = null;
			if (qt == QuantifierType.ALL)
			{
				new_child = new UniversalNode(level);
			}
			else
			{
				new_child = new ExistentialNode(level);
			}
			new_child.m_parentIdentifiers.addAll(parent_ids);
			for (Object id : m_seenIdentifiers)
			{
				Set<Object> new_parent_ids = new HashSet<Object>();
				new_parent_ids.addAll(parent_ids);
				new_parent_ids.add(id);
				QuantifierNode n = copy(level + 1, new_trace_id, new_parent_ids);
				if (n != null)
				{
					new_child.m_children.add(new QuantifierEdge(new_child, id, n));
				}
			}
			new_child.m_seenIdentifiers.addAll(m_seenIdentifiers);
			return new_child;
		}
	}

	/**
	 * A labelled edge between two quantifier nodes.
	 */
	protected class QuantifierEdge
	{
		/**
		 * The trace identifier this quantifier edge is associated with.
		 */
		/*@ non_null @*/ protected final Object m_traceIdentifier;

		/**
		 * The source node of this edge.
		 */
		/*@ non_null @*/ protected final QuantifierNode m_source;

		/**
		 * The destination node of this edge.
		 */
		/*@ non_null @*/ protected final QuantifierNode m_destination;

		/**
		 * Creates a new quantifier edge.
		 * @param source The source node of this edge
		 * @param trace_id The trace identifier this quantifier edge is associated
		 * with
		 * @param destination The destination node of this edge
		 */
		public QuantifierEdge(/*@ non_null @*/ QuantifierNode source, /*@ non_null @*/ Object trace_id, /*@ non_null @*/ QuantifierNode destination)
		{
			super();
			m_source = source;
			m_traceIdentifier = trace_id;
			m_destination = destination;
			m_destination.setParentEdge(this);
		}

		/**
		 * Determines if this edge has the same label as a given trace identifier.
		 * @param id The trace identifier
		 * @return {@code true} if the edge has the same label, {@code false}
		 * otherwise
		 */
		/*@ pure @*/ public boolean isFor(/*@ null @*/ Object id)
		{
			return m_traceIdentifier.equals(id);
		}

		/**
		 * Gets the source node of this edge.
		 * @return The node
		 */
		/*@ pure non_null @*/ public QuantifierNode getSource()
		{
			return m_source;
		}

		/**
		 * Gets the label node of this edge.
		 * @return The label
		 */
		/*@ pure non_null @*/ public Object getLabel()
		{
			return m_traceIdentifier;
		}

		/**
		 * Gets the destination node of this edge.
		 * @return The node
		 */
		/*@ pure non_null @*/ public QuantifierNode getDestination()
		{
			return m_destination;
		}

		@Override
		public String toString()
		{
			return m_traceIdentifier.toString();
		}
	}

	/**
	 * A quantifier node applying universal quantification.
	 */
	protected class UniversalNode extends QuantifierNode
	{
		/**
		 * Creates a new universal node.
		 * @param level The nesting level of this quantifier
		 */
		public UniversalNode(int level)
		{
			super(level);
		}

		@Override
		public Value getVerdict()
		{
			if (m_children.isEmpty())
			{
				return null;
			}
			boolean all_true = true;
			for (QuantifierEdge e : m_children)
			{
				Value v = e.getDestination().getVerdict();
				if (v == Value.FALSE)
				{
					return Value.FALSE;
				}
				if (v == null || v == Value.INCONCLUSIVE)
				{
					all_true = false;
				}
			}
			return all_true ? Value.TRUE : Value.INCONCLUSIVE;
		}

		@Override
		protected String getSymbol()
		{
			return "\u2200";
		}
	}

	/**
	 * A quantifier node applying universal quantification.
	 */
	protected class ExistentialNode extends QuantifierNode
	{
		/**
		 * Creates a new existential node.
		 * @param level The nesting level of this quantifier
		 */
		public ExistentialNode(int level)
		{
			super(level);
		}

		@Override
		public Value getVerdict()
		{
			if (m_children.isEmpty())
			{
				return null;
			}
			boolean all_false = true;
			for (QuantifierEdge e : m_children)
			{
				Value v = e.getDestination().getVerdict();
				if (v == Value.TRUE)
				{
					return Value.TRUE;
				}
				if (v == null || v == Value.INCONCLUSIVE)
				{
					all_false = false;
				}
			}
			return all_false ? Value.FALSE : Value.INCONCLUSIVE;
		}

		@Override
		protected String getSymbol()
		{
			return "\u2203";
		}
	}

	protected class LeafNode extends QuantifierNode
	{
		/*@ non_null @*/ protected final Pushable[] m_pushables;

		/*@ non_null @*/ protected final Processor m_phiInstance;

		/*@ non_null @*/ protected final QueueSink m_sink;
		
		/*@ non_null @*/ protected final CompletedPassthrough[] m_completed;

		protected boolean m_allCompleted;

		/*@ null @*/ protected Troolean.Value m_verdict;

		LeafNode(int level)
		{
			super(level);
			m_phiInstance = Quantify.this.m_phi.duplicate();
			m_pushables = new Pushable[m_phiInstance.getInputArity()];
			m_completed = new CompletedPassthrough[m_pushables.length];
			for (int i = 0; i < m_pushables.length; i++)
			{
				m_completed[i] = new CompletedPassthrough(1);
				Connector.connect(m_completed[i], 0, m_phiInstance, i);
				m_pushables[i] = m_completed[i].getPushableInput(0);
			}
			m_allCompleted = false;
			m_sink = new QueueSink();
			Connector.connect(m_phiInstance, m_sink);
			m_verdict = null;
		}

		@Override
		protected String getSymbol()
		{
			return "P";
		}

		@Override
		protected void push(LogUpdate event, Set<Integer> positions)
		{
			Object o = event.getEvent();
			for (int i : positions)
			{
				if (o == null)
				{
					m_pushables[i].notifyEndOfTrace();
				}
				else
				{
					m_pushables[i].push(o);
				}
			}
			m_allCompleted = allCompleted();
		}

		protected boolean allCompleted()
		{
			for (CompletedPassthrough cp : m_completed)
			{
				if (!cp.seenEndOfTrace())
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public Value getVerdict()
		{
			if (m_onlyCompleted && !m_allCompleted)
			{
				return null;
			}
			Queue<?> q = m_sink.getQueue();
			if (!q.isEmpty())
			{
				m_verdict = (Value) q.remove();
			}
			return m_verdict;
		}
	}
}
