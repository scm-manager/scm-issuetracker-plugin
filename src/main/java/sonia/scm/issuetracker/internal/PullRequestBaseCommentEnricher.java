package sonia.scm.issuetracker.internal;

import com.cloudogu.scm.review.comment.service.BasicComment;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Repository;

class PullRequestBaseCommentEnricher<T extends BasicComment> implements HalEnricher  {

  private final Class<T> commentType;
  private final IssueTracker issueTracker;
  private final PullRequestCommentMapper mapper;

  protected PullRequestBaseCommentEnricher(Class<T> commentType, IssueTracker issueTracker, PullRequestCommentMapper mapper) {
    this.commentType = commentType;
    this.issueTracker = issueTracker;
    this.mapper = mapper;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    BasicComment comment = context.oneRequireByType(commentType);

    HalAppender.LinkArrayBuilder builder = appender.linkArrayBuilder("issues");
    IssueReferencingObject ref = mapper.ref(repository, comment);
    issueTracker.findIssues(ref).forEach(builder::append);
    builder.build();
  }
}
