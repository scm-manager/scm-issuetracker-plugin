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
import sonia.scm.issuetracker.api.IssueReferencingObject;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.internal.resubmit.ResubmitQueue;
import sonia.scm.repository.Repository;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Builder for the default {@link IssueTracker} implementation. Start by calling
 * {@link #start(String, IssueMatcher, IssueLinkFactory)}, setting the least required
 * fields.
 *
 * @since 3.0.0
 */
public class IssueTrackerBuilder {

  private final DataStoreFactory dataStoreFactory;
  private final ResubmitQueue resubmitQueue;
  private final TemplateCommentRendererFactory templateCommentRendererFactory;

  @Inject
  public IssueTrackerBuilder(DataStoreFactory dataStoreFactory, ResubmitQueue resubmitQueue, TemplateCommentRendererFactory templateCommentRendererFactory) {
    this.dataStoreFactory = dataStoreFactory;
    this.resubmitQueue = resubmitQueue;
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
    return new ReadStage(this, name, matcher, linkFactory);
  }

  public static class ReadStage {

    private final IssueTrackerBuilder builder;
    private final String name;
    private final IssueMatcher matcher;
    private final IssueLinkFactory linkFactory;

    private ReadStage(IssueTrackerBuilder builder, String name, IssueMatcher matcher, IssueLinkFactory linkFactory) {
      this.builder = builder;
      this.name = name;
      this.matcher = matcher;
      this.linkFactory = linkFactory;
    }

    /**
     * Creates an {@link IssueTracker} that will only enrich links of objects and does not create comments
     * or trigger state changes.
     */
    public IssueTracker build() {
      return new DefaultIssueTracker(name, matcher, linkFactory);
    }

    /**
     * Sets required fields for creating comments in issues.
     *
     * @param repository repository from the {@link sonia.scm.issuetracker.api.IssueReferencingObject}.
     * @param commentator commentator that will be used to add comments to issues
     *
     * @return commenting stage
     */
    public CommentingStage commenting(Repository repository, Commentator commentator) {
      Preconditions.checkNotNull(repository, "repository is required");
      Preconditions.checkNotNull(commentator, "commentator is required");
      DataStore<ProcessedMarks> dataStore = createStore(repository);
      return new CommentingStage(this, repository, new ProcessedStore(dataStore), commentator);
    }

    private DataStore<ProcessedMarks> createStore(Repository repository) {
      return builder.dataStoreFactory.withType(ProcessedMarks.class)
        .withName("issueTracker" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1))
        .forRepository(repository)
        .build();
    }
  }

  public static class CommentingStage {

    private final ReadStage readStage;
    private final Repository repository;
    private final ProcessedStore store;
    private final ResubmittingCommentator commentator;

    public CommentingStage(ReadStage readStage, Repository repository, ProcessedStore store, Commentator commentator) {
      this.readStage = readStage;
      this.repository = repository;
      this.store = store;
      this.commentator = new ResubmittingCommentator(queue(), commentator);
    }

    private ResubmitRepositoryQueue queue() {
      return new ResubmitRepositoryQueue(readStage.builder.resubmitQueue, repository.getId(), readStage.name);
    }

    /**
     * Specify the renderer for the issue comments.
     *
     * @param renderer comment renderer
     * @return change state stage of builder
     */
    public ChangeStateStage renderer(ReferenceCommentRenderer renderer) {
      Preconditions.checkNotNull(renderer, "renderer is required");
      return new ChangeStateStage(this, renderer);
    }

    /**
     * Render issue comments using templates that will be read from a path build using the given
     * resource path template.
     * This resource path template can contain place holders for the type of the comment:
     * <code>{0}</code> will be replaced with {@link IssueReferencingObject#getType()}.
     *
     * @param resourcePathTemplate template for the resource path on the classpath
     *
     * @return change state stage of builder
     */
    public ChangeStateStage template(String resourcePathTemplate) {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(resourcePathTemplate), "resourcePathTemplate is required");
      ReferenceCommentRenderer renderer = readStage.builder.templateCommentRendererFactory.reference(resourcePathTemplate);
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
     * Allow changing states of issues by key words in the same sentence as an issue key
     * (e.g. "This closes #42").
     *
     * @param stateChanger state changer that will be used to change the state of an issue
     *
     * @return commenting state of builder
     */
    public ChangeStateRenderStage stateChanging(StateChanger stateChanger) {
      Preconditions.checkNotNull(stateChanger, "stateChanger is required");
      return new ChangeStateRenderStage(this, stateChanger);
    }

    /**
     * Creates {@link IssueTracker} which is able to create comments for issues.
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
     *
     * @param renderer comment renderer
     * @return change state stage of builder
     */
    public FinalStage renderer(StateChangeCommentRenderer renderer) {
      Preconditions.checkNotNull(renderer, "renderer is required");
      return new FinalStage(this, renderer);
    }

    /**
     * Render issue comments for state changes using templates that will be read from a path
     * build using the given resource path template.
     * This resource path template can contain place holders for the type of the comment:
     * <code>{0}</code> gets replaced with {@link IssueReferencingObject#getType()}.
     *
     * @param resourcePathTemplate template for the resource path on the classpath
     *
     * @return final stage of builder
     */
    public FinalStage template(String resourcePathTemplate) {
      Preconditions.checkArgument(!Strings.isNullOrEmpty(resourcePathTemplate), "resourcePathTemplate is required");
      StateChangeCommentRenderer renderer = changeStateStage.commentingStage.readStage.builder.templateCommentRendererFactory.stateChange(resourcePathTemplate);
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
     * Creates {@link IssueTracker} which is able to comment and change the state of issues.
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
