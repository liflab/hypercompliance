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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Unit tests for {@link XesToLog}.
 */
public class XesToLogTest
{
  @Test
  public void test1() throws IOException
  {
    XesToLog x = new XesToLog("Rfp_id");
    InputStream is = XesToLogTest.class.getResourceAsStream("resource/parsingtest.xes");
    Log log = x.getLog(is);
    is.close();
    assertEquals(1, log.size());
  }
}
