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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "name can not be null or empty");
    Preconditions.checkNotNull(matcher, "matcher is required");
    Preconditions.checkNotNull(linkFactory, "link factory is required");
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
      Preconditions.checkNotNull(repository, "repository is required");
      Preconditions.checkNotNull(commentator, "commentator is required");
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
    public ChangeStateStage renderer(ReferenceCommentRenderer renderer) {
      Preconditions.checkNotNull(renderer, "renderer is required");
      return new ChangeStateStage(this, renderer);
    }

    /**
     * Render issue comments with templates.
     * The template are read from the given resource path.
     * The resource path can contain place holders for type of object and the type of comment-
     * - {0} gets replaced with type of {@link sonia.scm.issuetracker.api.IssueReferencingObject}
     * @param resourcePathTemplate resource path template on the classpath
     * @return change state stage of builder
     */
    public ChangeStateStage template(String resourcePathTemplate) {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(resourcePathTemplate), "resourcePathTemplate is required");
      ReferenceCommentRenderer renderer = readStage.templateCommentRendererFactory.reference(resourcePathTemplate);
      return new ChangeStateStage(this, renderer);
    }

  }

  public static class ChangeStateStage {

    private final CommentingStage commentingStage;
    private final ReferenceCommentRenderer renderer;

    private ChangeStateStage(CommentingStage commentingStage, ReferenceCommentRenderer renderer) {
      this.commentingStage = commentingStage;
      this.renderer = renderer;
    }

    /**
     * Allow changing of an issue state by mention an key word in the same sentence as the issue key.
     *
     * @param stateChanger state changer which is able to change the state of an issue
     *
     * @return commenting state of builder
     */
    public ChangeStateRenderStage stateChanging(StateChanger stateChanger) {
      Preconditions.checkNotNull(stateChanger, "stateChanger is required");
      return new ChangeStateRenderStage(this, stateChanger);
    }

    /**
     * Creates {@link IssueTracker} which able to comment issues.
     *
     * @return new issue tracker
     */
    public IssueTracker build() {
      return new DefaultIssueTracker(
        commentingStage.readStage.name,
        commentingStage.readStage.matcher,
        commentingStage.readStage.linkFactory,
        commentingStage.store,
        renderer,
        commentingStage.commentator
      );
    }
  }

  public static class ChangeStateRenderStage {

    private final ChangeStateStage changeStateStage;
    private final StateChanger stateChanger;

    private ChangeStateRenderStage(ChangeStateStage changeStateStage, StateChanger stateChanger) {
      this.changeStateStage = changeStateStage;
      this.stateChanger = stateChanger;
    }

    /**
     * Specify the renderer for the issue comments.
     * @param renderer comment renderer
     * @return change state stage of builder
     */
    public FinalStage renderer(StateChangeCommentRenderer renderer) {
      Preconditions.checkNotNull(renderer, "renderer is required");
      return new FinalStage(this, renderer);
    }

    /**
     * Render state change issue comments with templates.
     * The template are read from the given resource path.
     * The resource path can contain place holders for type of object and the type of comment-
     * - {0} gets replaced with type of {@link sonia.scm.issuetracker.api.IssueReferencingObject}
     * @param resourcePathTemplate resource path template on the classpath
     * @return change state stage of builder
     */
    public FinalStage template(String resourcePathTemplate) {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(resourcePathTemplate), "resourcePathTemplate is required");
      StateChangeCommentRenderer renderer = changeStateStage.commentingStage.readStage.templateCommentRendererFactory.stateChange(resourcePathTemplate);
      return new FinalStage(this, renderer);
    }

  }

  public static class FinalStage {

    private final ChangeStateRenderStage changeStateRenderStage;
    private final StateChangeCommentRenderer renderer;

    private FinalStage(ChangeStateRenderStage changeStateRenderStage, StateChangeCommentRenderer renderer) {
      this.changeStateRenderStage = changeStateRenderStage;
      this.renderer = renderer;
    }

    /**
     * Creates {@link IssueTracker} which able to comment and change the state of issues.
     *
     * @return new issue tracker
     */
    public IssueTracker build() {
      return new DefaultIssueTracker(
        changeStateRenderStage.changeStateStage.commentingStage.readStage.name,
        changeStateRenderStage.changeStateStage.commentingStage.readStage.matcher,
        changeStateRenderStage.changeStateStage.commentingStage.readStage.linkFactory,
        changeStateRenderStage.changeStateStage.commentingStage.store,
        changeStateRenderStage.changeStateStage.renderer,
        changeStateRenderStage.changeStateStage.commentingStage.commentator,
        renderer,
        changeStateRenderStage.stateChanger
      );
    }

  }

}
