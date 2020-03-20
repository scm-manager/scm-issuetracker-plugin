/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.issuetracker;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.BasicPropertiesAware;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class PropertyUtilTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testGetBooleanProperty()
  {
    BasicPropertiesAware props = new BasicPropertiesAware();

    props.setProperty("p1", "asd");
    props.setProperty("p2", "false");
    props.setProperty("p3", "FALSE");
    props.setProperty("p4", "true");
    props.setProperty("p5", "TRUE");

    assertFalse(PropertyUtil.getBooleanProperty(props, "p0"));
    assertFalse(PropertyUtil.getBooleanProperty(props, "p1"));
    assertFalse(PropertyUtil.getBooleanProperty(props, "p2"));
    assertFalse(PropertyUtil.getBooleanProperty(props, "p3"));
    assertTrue(PropertyUtil.getBooleanProperty(props, "p4"));
    assertTrue(PropertyUtil.getBooleanProperty(props, "p5"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetEncryptedProperty()
  {
    String val = EncryptionUtil.encrypt("hansolo");
    BasicPropertiesAware props = new BasicPropertiesAware();

    props.setProperty("p1", "hansolo");
    props.setProperty("p2", val);

    assertEquals("hansolo", PropertyUtil.getEncryptedProperty(props, "p1"));
    assertEquals("hansolo", PropertyUtil.getEncryptedProperty(props, "p2"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetSetProperty()
  {
    BasicPropertiesAware props = new BasicPropertiesAware();

    props.setProperty("p1", "hansolo,chewbacca");
    props.setProperty("p2", "hansolo");

    Set<String> set = PropertyUtil.getSetProperty(props, "p1");

    assertThat(set, containsInAnyOrder("hansolo", "chewbacca"));

    set = PropertyUtil.getSetProperty(props, "p2");
    assertThat(set, containsInAnyOrder("hansolo"));
  }
}
