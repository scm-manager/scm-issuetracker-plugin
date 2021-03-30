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

package sonia.scm.issuetracker.spi;

import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.issuetracker.IssueMatcher;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;

/**
 * Builder for the default implementation {@link IssueTracker}.
 *
 * @since 3.0.0
 */
public class IssueTrackerBuilder {

  private final DataStoreFactory dataStoreFactory;
  private final TemplateCommentRendererFactory templateCommentRendererFactory;

  @Inject
  public IssueTrackerBuilder(DataStoreFactory dataStoreFactory, TemplateCommentRendererFactory templateCommentRendererFactory) {
    this.dataStoreFactory = dataStoreFactory;
    this.templateCommentRendererFactory = templateCommentRendererFactory;
  }

  /**
   * Create {@link ReadStage} of the builder.
   *
   * @param name name of the tracker
   * @param matcher matcher to match issue keys in content
   * @param linkFactory factory to create links for issues
   *
   * @return read stage
   */
  public ReadStage start(String name, IssueMatcher matcher, IssueLinkFactory linkFactory) {
    return new ReadStage(dataStoreFactory, templateCommentRendererFactory, name, matcher, linkFactory);
  }

  public static class ReadStage {

    private final DataStoreFactory dataStoreFactory;
    private final TemplateCommentRendererFactory templateCommentRendererFactory;
    private final String name;
    private final IssueMatcher matcher;
    private final IssueLinkFactory linkFactory;

    private ReadStage(DataStoreFactory dataStoreFactory, TemplateCommentRendererFactory templateCommentRendererFactory, String name, IssueMatcher matcher, IssueLinkFactory linkFactory) {
      this.dataStoreFactory = dataStoreFactory;
      this.templateCommentRendererFactory = templateCommentRendererFactory;
      this.name = name;
      this.matcher = matcher;
      this.linkFactory = linkFactory;
    }

    /**
     * Creates an {@link IssueTracker} which only enrich links of objects and does not comment or change state.
     *
     * @return read only issue tracker
     */
    public IssueTracker build() {
      return new DefaultIssueTracker(name, matcher, linkFactory);
    }

    /**
     * Allow commenting of issues.
     *
     * @param repository repository which is target
     * @param commentator commentator which is able to add comments to issues
     *
     * @return commenting stage
     */
    public CommentingStage commenting(Repository repository, Commentator commentator) {
      DataStore<ProcessedMarks> dataStore = createStore(repository);
      return new CommentingStage(this, new ProcessedStore(dataStore), commentator);
    }

    private DataStore<ProcessedMarks> createStore(Repository repository) {
      return dataStoreFactory.withType(ProcessedMarks.class)
        .withName("issue-tracker-" + name)
        .forRepository(repository).build();
    }
  }

  public static class CommentingStage {

    private final ReadStage readStage;
    private final ProcessedStore store;
    private final Commentator commentator;

    public CommentingStage(ReadStage readStage, ProcessedStore store, Commentator commentator) {
      this.readStage = readStage;
      this.store = store;
      this.commentator = commentator;
    }

    /**
     * Specify the renderer for the issue comments.
     * @param renderer comment renderer
     * @return change state stage of builder
     */
    public ChangeStateStage renderer(CommentRenderer renderer) {
      return new ChangeStateStage(readStage, store, renderer, commentator);
    }

    /**
     * Render issue comments with templates.
     * The template are read from the given resource path.
     * The resource path can contain place holders for type of object and the type of comment-
     * - {0} gets replaced with type of {@link sonia.scm.issuetracker.api.IssueReferencingObject}
     * - {1} gets replaced with the type of comment: reference or statechange
     * @param resourcePathTemplate resource path template on the classpath
     * @return change state stage of builder
     */
    public ChangeStateStage template(String resourcePathTemplate) {
      CommentRenderer renderer = readStage.templateCommentRendererFactory.create(resourcePathTemplate);
      return new ChangeStateStage(readStage, store, renderer, commentator);
    }

  }

  public static class ChangeStateStage {

    private final ReadStage readStage;
    private final ProcessedStore store;
    private final CommentRenderer renderer;
    private final Commentator commentator;
    private StateChanger stateChanger;

    private ChangeStateStage(ReadStage readStage, ProcessedStore store, CommentRenderer renderer, Commentator commentator) {
      this.readStage = readStage;
      this.store = store;
      this.renderer = renderer;
      this.commentator = commentator;
    }

    /**
     * Allow changing of an issue state by mention an key word in the same sentence as the issue key.
     *
     * @param stateChanger state changer which is able to change the state of an issue
     *
     * @return commenting state of builder
     */
    public ChangeStateStage stateChanging(StateChanger stateChanger) {
      this.stateChanger = stateChanger;
      return this;
    }

    /**
     * Creates {@link IssueTracker} which able to comment issues and optional to change the state of an issue.
     *
     * @return new issue tracker
     */
    public IssueTracker build() {
      return new DefaultIssueTracker(
        readStage.name,
        readStage.matcher,
        readStage.linkFactory,
        store,
        renderer,
        commentator,
        stateChanger
      );
    }
  }

}
