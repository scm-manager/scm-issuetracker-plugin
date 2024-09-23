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

package sonia.scm.issuetracker.internal.review;


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
