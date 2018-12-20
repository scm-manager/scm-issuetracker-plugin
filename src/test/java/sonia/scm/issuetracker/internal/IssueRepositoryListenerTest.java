/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
