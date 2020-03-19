/**
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
