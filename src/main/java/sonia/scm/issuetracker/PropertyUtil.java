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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import sonia.scm.PropertiesAware;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.1
 */
public final class PropertyUtil
{

  /** Field description */
  private static final String SEPARATOR = ",";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private PropertyUtil() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param props
   * @param key
   *
   * @return
   */
  public static boolean getBooleanProperty(PropertiesAware props, String key)
  {
    boolean result = false;
    String value = props.getProperty(key);

    if (Util.isNotEmpty(value))
    {
      result = Boolean.parseBoolean(value);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param props
   * @param key
   *
   * @return
   */
  public static String getEncryptedProperty(PropertiesAware props, String key)
  {
    String value = props.getProperty(key);

    if (EncryptionUtil.isEncrypted(value))
    {
      value = EncryptionUtil.decrypt(value);
    }

    return value;
  }

  /**
   * Method description
   *
   *
   * @param props
   * @param key
   *
   * @return
   */
  public static Set<String> getSetProperty(PropertiesAware props, String key)
  {
    Set<String> values;
    String value = props.getProperty(key);

    if (!Strings.isNullOrEmpty(value))
    {
      //J-
      values = ImmutableSet.copyOf(
        Splitter.on(SEPARATOR).trimResults().omitEmptyStrings().split(value)
      );
      //J+
    }
    else
    {
      values = Collections.EMPTY_SET;
    }

    return values;
  }
}
