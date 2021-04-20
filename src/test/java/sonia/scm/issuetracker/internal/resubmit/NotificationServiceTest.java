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
