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

package sonia.scm.issuetracker.internal;


import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sonia.scm.HandlerEventType;
import sonia.scm.event.HandlerEvent;

import static org.assertj.core.api.Assertions.assertThat;

class PullRequestEventsTest {

  @Test
  void shouldReturnTrue() {
    assertType(HandlerEventType.CREATE).isTrue();
    assertType(HandlerEventType.MODIFY).isTrue();
  }

  private AbstractBooleanAssert<?> assertType(HandlerEventType type) {
    return assertThat(PullRequestEvents.isSupported(new SimleHandlerEvent(type)));
  }

  @ParameterizedTest(name = "should return false for {argumentsWithNames}")
  @EnumSource(value = HandlerEventType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CREATE", "MODIFY"})
  void shouldReturnFalse(HandlerEventType eventType) {
    assertType(eventType).isFalse();
  }

  public class SimleHandlerEvent implements HandlerEvent<String> {

    private HandlerEventType eventType;

    public SimleHandlerEvent(HandlerEventType eventType) {
      this.eventType = eventType;
    }

    @Override
    public HandlerEventType getEventType() {
      return eventType;
    }

    @Override
    public String getItem() {
      return null;
    }

    @Override
    public String getOldItem() {
      return null;
    }
  }
}
