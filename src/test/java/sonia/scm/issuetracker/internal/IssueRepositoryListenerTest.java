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
