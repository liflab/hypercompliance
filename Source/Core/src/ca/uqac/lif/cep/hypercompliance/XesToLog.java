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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import ca.uqac.lif.cep.tuples.Tuple;
import ca.uqac.lif.cep.tuples.TupleMap;

public class XesToLog
{
  protected final XesXmlParser m_parser;
  
  protected final String m_caseId;
  
  public XesToLog(String case_id)
  {
    super();
    m_parser = new XesXmlParser();
    m_caseId = case_id;
  }
  
  public Log getLog(InputStream is)
  {
    XLog xlog = null;
    try
    {
      List<XLog> xlogs = m_parser.parse(is);
      xlog = xlogs.get(0);
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    Log log = new Log();
    for (XTrace xtrace : xlog)
    {
      XAttributeMap trace_atts = xtrace.getAttributes();
      XAttribute trace_id = trace_atts.get(m_caseId);
      for (XEvent event : xtrace)
      {
        log.appendTo(trace_id, Arrays.asList(eventToTuple(event)));
      }
    }
    return log;
  }
  
  protected static Tuple eventToTuple(XEvent e)
  {
    XAttributeMap attr_map = e.getAttributes();
    TupleMap tuple = new TupleMap();
    for (Map.Entry<String,XAttribute> entry : attr_map.entrySet())
    {
      tuple.put(entry.getKey(), castValue((XAttributeImpl) entry.getValue()));
    }
    return tuple;
  }
  
  protected static Object castValue(XAttributeImpl o)
  {
    if (o instanceof XAttributeDiscreteImpl)
    {
      // Long
      return ((XAttributeDiscreteImpl) o).getValue();
    }
    if (o instanceof XAttributeLiteralImpl)
    {
      // String
      return ((XAttributeLiteralImpl) o).getValue();
    }
    if (o instanceof XAttributeBooleanImpl)
    {
      // Boolean
      return ((XAttributeBooleanImpl) o).getValue();
    }
    if (o instanceof XAttributeContinuousImpl)
    {
      // Double
      return ((XAttributeContinuousImpl) o).getValue();
    }
    if (o instanceof XAttributeTimestampImpl)
    {
      // Milliseconds
      return ((XAttributeTimestampImpl) o).getValueMillis();
    }
    return null;
  }
  
  /**
   * Adds an XES string attribute to a tuple map.
   * @param tuple The tuple where the attribute must be added
   * @param attribute The XES attribute to read data from
   */
  protected static void addStringAttribute(TupleMap tuple, XAttribute attribute)
  {
    System.out.println(attribute);
  }
  
  /**
   * Adds an XES numerical attribute to a tuple map.
   * @param tuple The tuple where the attribute must be added
   * @param attribute The XES attribute to read data from
   */
  protected static void addNumericalAttribute(TupleMap tuple, XAttribute attribute)
  {
    
  }
  
  /**
   * Adds an XES date/time attribute to a tuple map.
   * @param tuple The tuple where the attribute must be added
   * @param attribute The XES attribute to read data from
   */
  protected static void addDateAttribute(TupleMap tuple, XAttribute attribute)
  {
    
  }
}
