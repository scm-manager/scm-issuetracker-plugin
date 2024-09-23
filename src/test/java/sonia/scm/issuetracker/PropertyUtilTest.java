/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
