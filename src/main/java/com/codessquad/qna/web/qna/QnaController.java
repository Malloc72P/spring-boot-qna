package com.codessquad.qna.web.qna;

import com.codessquad.qna.web.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
public class QnaController {
    @Autowired
    private QuestionRepository questionRepository;

    Logger logger = LoggerFactory.getLogger(QnaController.class);

    @PostMapping("/questions")
    public String createQuestion(Question newQuestion, HttpSession session) {
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser != null) {
            newQuestion.setWriterId(sessionUser.getId());
            newQuestion.setWriterUserId(sessionUser.getUserId());
            questionRepository.save(newQuestion);
            logger.info("question created! [" + newQuestion.getId() + "] " + " title : " + newQuestion.getTitle());
        }
        return "redirect:/";
    }

    @GetMapping("/")
    public String getQuestionList(Model model) {
        model.addAttribute("questions", questionRepository.findAll());
        return "index";
    }

    @GetMapping("/questions/{questionId}")
    public String getOneQuestion(@PathVariable("questionId") long questionId, Model model) {
        Question foundQuestion = getQuestionById(questionId);
        model.addAttribute("question", foundQuestion);
        return "qna/show";
    }

    @GetMapping("/questions/modify/{questionId}")
    public String getModifyPage(@PathVariable("questionId") long questionId,
                                Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser == null) {
            return "redirect:/";
        }
        Question currentQuestion = getQuestionById(questionId);
        if (currentQuestion.getWriterId() != sessionUser.getId()) {
            return "redirect:/";
        }
        model.addAttribute("currentQuestion", currentQuestion);
        return "qna/modify-form";
    }

    @PutMapping("/questions/modify")
    public String modifyQuestion(long questionId, String newTitle, String newContents,
                                 Model model, HttpSession session) {
        Question currentQuestion = getQuestionById(questionId);
        if (currentQuestion == null) {
            return "redirect:/";
        }
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser == null) {
            return "redirect:/";
        }
        if (!currentQuestion.isMatchingWriterId(sessionUser.getId())) {
            return "redirect:/";
        }
        currentQuestion.setTitle(newTitle);
        currentQuestion.setContents(newContents);
        questionRepository.save(currentQuestion);
        logger.info("question modified! title : " + currentQuestion.getTitle());
        return "redirect:/questions/" + currentQuestion.getId();
    }

    @DeleteMapping("/questions/{questionId}")
    public String deleteQuestion(@PathVariable("questionId") long questionId, HttpSession session) {
        Question currentQuestion = getQuestionById(questionId);
        if (currentQuestion == null) {
            return "redirect:/";
        }
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser == null) {
            return "redirect:/";
        }
        if (!currentQuestion.isMatchingWriterId(sessionUser.getId())) {
            return "redirect:/";
        }
        logger.info("question deleted! title : " + currentQuestion.getTitle());
        questionRepository.delete(currentQuestion);
        return "redirect:/";
    }

    private Question getQuestionById(long questionId) {
        return (Question) questionRepository.findById(questionId).orElse(null);
    }

}
