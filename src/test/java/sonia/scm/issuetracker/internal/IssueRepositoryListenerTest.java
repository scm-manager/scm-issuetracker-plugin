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

package sonia.scm.issuetracker.internal;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import org.junit.Test;
import sonia.scm.HandlerEventType;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryTestData;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class IssueRepositoryListenerTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testHandleRepositoryEvent()
  {

    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    IssueTrackerManager manager = createIssueTrackerManager();

    IssueRepositoryListener listener = new IssueRepositoryListener(manager);
    EventBus eventBus = new EventBus();

    eventBus.register(listener);

    eventBus.post(new RepositoryEvent(HandlerEventType.DELETE, repository));
    eventBus.post(new RepositoryEvent(HandlerEventType.DELETE, repository));
    eventBus.post(new RepositoryEvent(HandlerEventType.DELETE, repository));

    for (IssueTracker tracker : manager.getIssueTrackers())
    {
      // TODO changesets cannot be received from the event bus
//      verify(tracker, times(3)).removeHandledMarks(repository);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @return
   */
  private IssueTrackerManager createIssueTrackerManager()
  {
    IssueTracker tracker1 = mockIssueTracker("tracker1");
    IssueTracker tracker2 = mockIssueTracker("tracker2");
    Set<IssueTracker> trackers = ImmutableSet.of(tracker1, tracker2);

    return new IssueTrackerManager(trackers);
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  private IssueTracker mockIssueTracker(String name)
  {
    IssueTracker tracker = mock(IssueTracker.class);

    when(tracker.getName()).thenReturn(name);

    return tracker;
  }
}
