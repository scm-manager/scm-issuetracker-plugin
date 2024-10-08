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

package sonia.scm.issuetracker.internal.resubmit;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;

import java.util.Collections;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  private NotificationService service;

  @Mock
  private ResubmitConfigurationStore store;

  @Mock
  private MailService mailService;

  private final QueuedComment commentOne = new QueuedComment("abc", "redmine", "#42", "Awesome");

  @BeforeEach
  void setUpService() {
    ScmConfiguration configuration = new ScmConfiguration();
    configuration.setBaseUrl("https://hitchhiker.com/scm");
    service = new NotificationService(store, configuration, mailService);
  }

  @Test
  void shouldNotSendNotificationIfMailServiceIsNotConfigured() {
    configuration(false, "trillian@hitchhiker.com");

    service.notifyComment(commentOne);

    verify(mailService, never()).emailTemplateBuilder();
  }

  @Test
  void shouldNotSendNotificationWithoutAddresses() {
    configuration(true);

    service.notifyComment(commentOne);

    verify(mailService, never()).emailTemplateBuilder();
  }

  @Mock
  private MailService.EnvelopeBuilder envelopeBuilder;

  @Mock
  private MailService.SubjectBuilder subjectBuilder;

  @Mock
  private MailService.TemplateBuilder templateBuilder;

  @Mock
  private MailService.MailBuilder mailBuilder;

  @Test
  void shouldSendNotificationForComment() throws MailSendBatchException {
    configuration(true, "trillian@hitchhiker.com");

    mockMailBuilder(NotificationService.TEMPLATE_COMMENT);

    service.notifyComment(commentOne);

    verify(envelopeBuilder).toAddress("trillian@hitchhiker.com");
    assertSubject(envelopeBuilder, "comment", "#42", "redmine");
    verify(mailBuilder).send();
  }

  @Test
  void shouldSendNotificationForResubmit() throws MailSendBatchException {
    configuration(true, "dent@hitchhiker.com");

    mockMailBuilder(NotificationService.TEMPLATE_RESUBMIT);

    service.notifyResubmit("jira", Collections.emptyList(), Collections.singleton(commentOne));

    verify(envelopeBuilder).toAddress("dent@hitchhiker.com");
    assertSubject(envelopeBuilder, "jira", "comments", "0", "1");
    verify(mailBuilder).send();
  }

  @Test
  void shouldSendNotificationsInEnglishAndGerman() {
    configuration(true, "dent@hitchhiker.com");

    mockMailBuilder(NotificationService.TEMPLATE_COMMENT);

    service.notifyComment(commentOne);

    verify(subjectBuilder).withSubject(eq(Locale.ENGLISH), anyString());
    verify(subjectBuilder).withSubject(eq(Locale.GERMAN), anyString());
  }

  @Test
  void shouldNotThrowException() throws MailSendBatchException {
    configuration(true, "trillian@hitchhiker.com");

    mockMailBuilder(NotificationService.TEMPLATE_COMMENT);
    doThrow(new MailSendBatchException("failed")).when(mailBuilder).send();

    service.notifyComment(commentOne);

    verify(envelopeBuilder).toAddress("trillian@hitchhiker.com");
    assertSubject(envelopeBuilder, "comment", "#42", "redmine");
    verify(mailBuilder).send();
  }

  private void mockMailBuilder(String template) {
    when(mailService.emailTemplateBuilder()).thenReturn(envelopeBuilder);
    when(envelopeBuilder.withSubject(any())).thenReturn(subjectBuilder);
    when(subjectBuilder.withTemplate(template, MailTemplateType.MARKDOWN_HTML)).thenReturn(templateBuilder);
    when(templateBuilder.andModel(any())).thenReturn(mailBuilder);
  }

  private void assertSubject(MailService.EnvelopeBuilder envelopeBuilder, String... contains) {
    ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
    verify(envelopeBuilder).withSubject(subjectCaptor.capture());
    String subject = subjectCaptor.getValue();
    assertThat(subject).contains(contains);
  }

  private void configuration(boolean mailServiceEnabled, String... addresses) {
    when(mailService.isConfigured()).thenReturn(mailServiceEnabled);

    ResubmitConfiguration configuration = new ResubmitConfiguration();
    configuration.setAddresses(ImmutableList.copyOf(addresses));
    when(store.get()).thenReturn(configuration);
  }
}
