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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

@Singleton
public class NotificationService {

  private static final String SUBJECT_BUNDLE = "sonia.scm.issuetracker.internal.resubmit.subjects";
  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final Set<Locale> LOCALES = ImmutableSet.of(DEFAULT_LOCALE, Locale.GERMAN);

  @VisibleForTesting
  static final String TEMPLATE_COMMENT = "/sonia/scm/issuetracker/internal/resubmit/comment-mail.mustache";

  @VisibleForTesting
  static final String TEMPLATE_RESUBMIT = "/sonia/scm/issuetracker/internal/resubmit/resubmit-mail.mustache";

  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private final ResubmitConfigurationStore store;
  private final ScmConfiguration configuration;
  private final MailService mailService;
  private final Map<Locale, ResourceBundle> subjects;

  @Inject
  public NotificationService(ResubmitConfigurationStore store, ScmConfiguration configuration, MailService mailService) {
    this.store = store;
    this.configuration = configuration;
    this.mailService = mailService;

    subjects = Maps.asMap(LOCALES, locale -> ResourceBundle.getBundle(SUBJECT_BUNDLE, locale));
  }

  public void notifyComment(QueuedComment comment) {
    notify(TEMPLATE_COMMENT, new CommentModel(comment, createResubmitUrl()), bundle -> {
      String subject = bundle.getString("comment");
      return MessageFormat.format(subject, comment.getIssueTracker(), comment.getIssueKey());
    });
  }

  private String createResubmitUrl() {
    return HttpUtil.concatenate(configuration.getBaseUrl(), "admin", "issue-tracker");
  }

  public void notifyResubmit(String issueTracker, Collection<QueuedComment> remove, Collection<QueuedComment> requeue) {
    notify(TEMPLATE_RESUBMIT, new ResubmitModel(issueTracker, remove.size(), requeue.size(), createResubmitUrl()), bundle -> {
      String subject = bundle.getString("resubmit");
      return MessageFormat.format(subject, issueTracker, remove.size(), requeue.size());
    });
  }

  private void notify(String template, Object model, Function<ResourceBundle, String> subjectSupplier) {
    List<String> addresses = store.get().getAddresses();
    if (shouldSendNotification(addresses)) {

      MailService.EnvelopeBuilder envelopeBuilder = mailService.emailTemplateBuilder();
      for (String address : addresses) {
        envelopeBuilder.toAddress(address);
      }

      ResourceBundle resourceBundle = subjects.get(DEFAULT_LOCALE);
      String subject = subjectSupplier.apply(resourceBundle);

      MailService.SubjectBuilder subjectBuilder = envelopeBuilder.withSubject(subject);
      for (Map.Entry<Locale, ResourceBundle> e : subjects.entrySet()) {
        subjectBuilder.withSubject(e.getKey(), subjectSupplier.apply(e.getValue()));
      }

      MailService.MailBuilder mailBuilder = subjectBuilder.withTemplate(template, MailTemplateType.MARKDOWN_HTML)
        .andModel(model);

      send(mailBuilder);
    }
  }

  private boolean shouldSendNotification(List<String> addresses) {
    return mailService.isConfigured() && ! addresses.isEmpty();
  }

  private void send(MailService.MailBuilder mailBuilder) {
    try {
      mailBuilder.send();
    } catch (MailSendBatchException e) {
      LOG.warn("failed to send notification", e);
    }
  }

  @Value
  @VisibleForTesting
  public static class CommentModel {
    QueuedComment comment;
    String url;
  }

  @Value
  @VisibleForTesting
  public static class ResubmitModel {
    String issueTracker;
    int removeCount;
    int requeueCount;
    String url;
  }
}
