package com.codessquad.qna.web.answers;

import com.codessquad.qna.web.questions.Question;
import com.codessquad.qna.web.users.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_answer_writer"))
    private User writer;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_answer_to_question"))
    private Question question;

    @Column(nullable = false, length = 400)
    private String contents;

    private LocalDateTime reportingDateTime;

    public boolean isMatchingWriter(User anotherWriter) {
        return writer.isMatchingId(anotherWriter);
    }

    public Answer(String contents) {
        this.contents = contents;
        reportingDateTime = LocalDateTime.now();
    }

    public Answer(String contents, Question question, User writer) {
        this.contents = contents;
        this.question = question;
        this.writer = writer;
        reportingDateTime = LocalDateTime.now();
    }

    public Answer() {
        reportingDateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public User getWriter() {
        return writer;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public LocalDateTime getReportingDateTime() {
        return reportingDateTime;
    }

    public void setReportingDateTime(LocalDateTime reportingDateTime) {
        this.reportingDateTime = reportingDateTime;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", writer=" + writer +
                ", question=" + question +
                ", contents='" + contents + '\'' +
                ", reportingDateTime=" + reportingDateTime +
                '}';
    }
}
