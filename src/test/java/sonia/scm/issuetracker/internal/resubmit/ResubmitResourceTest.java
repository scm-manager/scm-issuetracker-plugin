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

package sonia.scm.issuetracker.internal.resubmit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.internal.IssueTrackerResource;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResubmitResourceTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private ResubmitQueue queue;

  @Mock
  private ResubmitDispatcher dispatcher;

  private RestDispatcher restDispatcher;

  @BeforeEach
  void setUp() {
    Provider<ResubmitResource> resubmitProvider = Providers.of(new ResubmitResource(queue, dispatcher));
    IssueTrackerResource resource = new IssueTrackerResource(resubmitProvider);

    restDispatcher = new RestDispatcher();
    restDispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldReturnSelfLink() throws IOException, URISyntaxException {
    comments();

    MockHttpResponse response = invoke(MockHttpRequest.get("/v2/issue-tracker/resubmits"));

    JsonNode node = mapper.readTree(response.getContentAsString());
    assertThat(node.get("_links").get("self").get("href").asText()).isEqualTo("/v2/issue-tracker/resubmits");
  }

  @Test
  void shouldReturnResubmits() throws IOException, URISyntaxException {
    comments("jira", "jira");

    MockHttpResponse response = invoke(MockHttpRequest.get("/v2/issue-tracker/resubmits"));

    JsonNode resubmit = mapper.readTree(response.getContentAsString()).get("_embedded").get("resubmit").get(0);
    assertThat(resubmit.get("issueTracker").asText()).isEqualTo("jira");
    assertThat(resubmit.get("queueSize").asInt()).isEqualTo(2);
    assertThat(resubmit.get("inProgress").asBoolean()).isFalse();

    JsonNode links = resubmit.get("_links");
    assertThat(links.get("resubmit").get("href").asText()).isEqualTo("/v2/issue-tracker/resubmits/jira/resubmit");
    assertThat(links.get("clear").get("href").asText()).isEqualTo("/v2/issue-tracker/resubmits/jira/clear");
  }

  @Test
  void shouldTriggerResubmit() throws URISyntaxException {
    MockHttpResponse response = invoke(MockHttpRequest.post("/v2/issue-tracker/resubmits/redmine/resubmit"));

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_ACCEPTED);
    verify(dispatcher).resubmitAsync("redmine");
  }

  @Test
  void shouldTriggerClear() throws URISyntaxException {
    MockHttpResponse response = invoke(MockHttpRequest.post("/v2/issue-tracker/resubmits/bugzilla/clear"));

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_ACCEPTED);
    verify(queue).clear("bugzilla");
  }

  private MockHttpResponse invoke(MockHttpRequest request) {
    MockHttpResponse response = new MockHttpResponse();
    restDispatcher.invoke(request, response);
    return response;
  }

  private void comments(String... issueTrackers) {
    Multimap<String, QueuedComment> comments = HashMultimap.create();
    for (String issueTracker : issueTrackers) {
      comments.put(issueTracker, comment(issueTracker));
    }
    when(queue.getComments()).thenReturn(comments);
  }

  private AtomicInteger counter;

  @BeforeEach
  void setUpCounter() {
    this.counter = new AtomicInteger();
  }

  private QueuedComment comment(String issueTracker) {
    return new QueuedComment("hog", issueTracker, "#" + counter.incrementAndGet(), "Super");
  }

}
