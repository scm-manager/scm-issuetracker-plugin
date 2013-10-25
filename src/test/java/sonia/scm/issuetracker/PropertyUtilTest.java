/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
