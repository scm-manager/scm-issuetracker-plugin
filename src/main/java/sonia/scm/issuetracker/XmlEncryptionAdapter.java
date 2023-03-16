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

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.1
 * @deprecated Use {@link sonia.scm.xml.XmlEncryptionAdapter} from SCM-Manager
 *   core instead. Otherwise, other plugins like the audit log cannot
 *   mask passwords correctly.
 *   The annotation from the core is compatible to this annotation, so
 *   no data has to be migrated.
 */
@Deprecated
public class XmlEncryptionAdapter extends XmlAdapter<String, String>
{

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String marshal(String v) throws Exception
  {
    if (!EncryptionUtil.isEncrypted(v))
    {
      v = EncryptionUtil.encrypt(v);
    }

    return v;
  }

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String unmarshal(String v) throws Exception
  {
    if (EncryptionUtil.isEncrypted(v))
    {
      v = EncryptionUtil.decrypt(v);
    }

    return v;
  }
}
