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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import sonia.scm.event.ScmEventBus;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueRequest;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.WrappedRepositoryHookEvent;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetResponse;
import sonia.scm.repository.spi.HookContextProvider;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Sebastian Sdorra
 */
public class IssuePostReceiveRepositoryHookTest
{

  /**
   * Method description
   *
   */
  @Test
  @SuppressWarnings("squid:S2925") // use Thread.sleep() to wait for the eventBus post process
  public void testHandleEvent() throws InterruptedException {
    Repository repository = RepositoryTestData.create42Puzzle();

    Changeset c1 = new Changeset();

    c1.setId("1");
    c1.setDescription("description without issue key");

    Changeset c2 = new Changeset();

    c2.setId("2");
    c2.setDescription("description with issue key SCM-42");

    IssueTracker jira = createIssueTracker("jira", new JiraIssueMatcher());
    IssueTrackerManager manager = createIssueTrackerManager(jira);

    ScmEventBus.getInstance().register(new IssuePostReceiveRepositoryHook(manager));
    ScmEventBus.getInstance().post(mockEvent(repository, c1, c2));

    Thread.sleep(3000);
    verify(jira, times(1)).isHandled(repository, c1);
    verify(jira, times(1)).isHandled(repository, c2);

    IssueRequest request = new IssueRequest(repository, c2,
                             Lists.newArrayList("SCM-42"));

    verify(jira, times(1)).handleRequest(request);
    verify(jira, times(1)).handleRequest(any(IssueRequest.class));

    verify(jira, never()).markAsHandled(repository, c1);
    verify(jira, times(1)).markAsHandled(repository, c2);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param matcher
   *
   * @return
   */
  private IssueTracker createIssueTracker(String name, IssueMatcher matcher)
  {
    IssueTracker tracker = mock(IssueTracker.class);

    when(tracker.getName()).thenReturn(name);
    when(tracker.createMatcher(any(Repository.class))).thenReturn(matcher);

    return tracker;
  }

  /**
   * Method description
   *
   *
   * @param trackers
   *
   * @return
   */
  private IssueTrackerManager createIssueTrackerManager(
    IssueTracker... trackers)
  {
    return new IssueTrackerManager(ImmutableSet.copyOf(trackers));
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private WrappedRepositoryHookEvent mockEvent(Repository repository, Changeset... changesets)
  {
    RepositoryHookEvent wrapped = mock(RepositoryHookEvent.class);

    when(wrapped.getRepository()).thenReturn(repository);
    HookChangesetProvider provider = mock(HookChangesetProvider.class);
    HookContextProvider hookContextProvider = mock(HookContextProvider.class);
    HookContext context = new HookContextFactory(null).createContext(hookContextProvider, repository);
    when(wrapped.getContext()).thenReturn(context);
    when(hookContextProvider.getChangesetProvider()).thenReturn(provider);
    HookChangesetResponse hookChangesetResponse = new HookChangesetResponse(ImmutableList.copyOf(changesets));
    when(provider.handleRequest(any())).thenReturn(hookChangesetResponse);
    when(hookContextProvider.getSupportedFeatures()).thenReturn(Sets.immutableEnumSet(HookFeature.CHANGESET_PROVIDER));
    when(wrapped.getType()).thenReturn(RepositoryHookType.POST_RECEIVE);

    return PostReceiveRepositoryHookEvent.wrap(wrapped);
  }
}
