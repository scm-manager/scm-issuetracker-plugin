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
import sonia.scm.repository.api.HookChangesetProvider;
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
