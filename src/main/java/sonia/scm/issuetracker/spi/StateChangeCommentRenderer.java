package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.api.IssueReferencingObject;

import java.io.IOException;

/**
 * Renders comments after a state of an issue was changed.
 *
 * @since 3.0.0
 */
public interface StateChangeCommentRenderer {

  /**
   * Creates a state change comment.
   *
   * @param object issue reference object
   * @param keyWord keyword which has triggered the state change
   * @return reference comment
   * @throws IOException failed to render comment
   */
  String render(IssueReferencingObject object, String keyWord) throws IOException;
}
