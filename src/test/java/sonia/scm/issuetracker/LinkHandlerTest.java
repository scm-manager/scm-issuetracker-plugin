package sonia.scm.issuetracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;

import java.util.Collections;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkHandlerTest {

  @Mock
  private ScmConfiguration configuration;

  @InjectMocks
  private LinkHandler linkHandler;

  @BeforeEach
  void setup() {
    when(configuration.getBaseUrl()).thenReturn("https://hitchhiker.com/scm");
  }

  @Test
  void getDiffUrl() {
    Changeset changeset = new Changeset();
    changeset.setId("1234567890");

    IssueRequest issueRequest = new IssueRequest(RepositoryTestData.createHeartOfGold(), changeset, Collections.emptyList(), of(new User()));
    String url = linkHandler.getDiffUrl(issueRequest);
    assertThat(url).isEqualTo("https://hitchhiker.com/scm/repo/hitchhiker/HeartOfGold/changeset/1234567890");

  }

  @Test
  void getRepositoryUrl() {
    Changeset changeset = new Changeset();
    changeset.setId("1234567890");

    IssueRequest issueRequest = new IssueRequest(RepositoryTestData.createHeartOfGold(), changeset, Collections.emptyList(), of(new User()));
    String url = linkHandler.getRepositoryUrl(issueRequest);
    assertThat(url).isEqualTo("https://hitchhiker.com/scm/repo/hitchhiker/HeartOfGold");
  }
}
