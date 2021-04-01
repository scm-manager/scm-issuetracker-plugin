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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.issuetracker.ExampleIssueMatcher;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.IssueRequest;
import sonia.scm.issuetracker.IssueTracker;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.spi.HookChangesetProvider;
import sonia.scm.repository.spi.HookChangesetResponse;
import sonia.scm.repository.spi.HookContextProvider;
import sonia.scm.user.User;

import static java.util.Optional.of;
import static org.mockito.Mockito.*;

/**
 *
 * @author Sebastian Sdorra
 */
@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/issuetracker/shiro.ini"
)
public class LegacyChangesetSubscriberTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final IssueTracker jira = createIssueTracker("jira", ExampleIssueMatcher.createJira());
  private final IssueTrackerManager manager = createIssueTrackerManager(jira);

  @Test
  public void testHandleEvent() {
    Subject subject = mock(Subject.class);
    SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(){
      @Override
      public <T> T oneByType(Class<T> type) {
        if (type.isAssignableFrom(User.class)) {
          return (T) new User();
        }
        return super.oneByType(type);
      }
    };
    when(subject.getPrincipals()).thenReturn(principalCollection);
    shiroRule.setSubject(subject);

    Repository repository = RepositoryTestData.create42Puzzle();

    Changeset c1 = new Changeset();

    c1.setId("1");
    c1.setDescription("description without issue key");

    Changeset c2 = new Changeset();

    c2.setId("2");
    c2.setDescription("description with issue key SCM-42");


    LegacyChangesetSubscriber legacyChangesetSubscriber = new LegacyChangesetSubscriber(manager);
    legacyChangesetSubscriber.handleEvent(mockEvent(repository, c1, c2));

    verify(jira, times(1)).isHandled(repository, c1);
    verify(jira, times(1)).isHandled(repository, c2);

    IssueRequest request = new IssueRequest(repository, c2, Lists.newArrayList("SCM-42"), of(new User()));

    verify(jira, times(1)).handleRequest(request);
    verify(jira, times(1)).handleRequest(any(IssueRequest.class));

    verify(jira, never()).markAsHandled(repository, c1);
    verify(jira, times(1)).markAsHandled(repository, c2);
  }

  @Test
  public void shouldNotFailWhenSubjectIsMissing() throws InterruptedException {
    Repository repository = RepositoryTestData.create42Puzzle();

    Changeset c1 = new Changeset();

    c1.setId("1");
    c1.setDescription("description without issue key");

    LegacyChangesetSubscriber legacyChangesetSubscriber = new LegacyChangesetSubscriber(manager);
    legacyChangesetSubscriber.handleEvent(mockEvent(repository, c1));

    verify(jira, times(1)).isHandled(repository, c1);
  }

  private IssueTracker createIssueTracker(String name, IssueMatcher matcher) {
    IssueTracker tracker = mock(IssueTracker.class);

    when(tracker.getName()).thenReturn(name);
    when(tracker.createMatcher(any(Repository.class))).thenReturn(of(matcher));

    return tracker;
  }

  private IssueTrackerManager createIssueTrackerManager(IssueTracker... trackers) {
    return new IssueTrackerManager(ImmutableSet.copyOf(trackers));
  }

  private PostReceiveRepositoryHookEvent mockEvent(Repository repository, Changeset... changesets) {
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

    return new PostReceiveRepositoryHookEvent(wrapped);
  }
}
