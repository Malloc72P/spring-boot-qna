package com.codessquad.qna.web.controller;

import com.codessquad.qna.web.domain.User;
import com.codessquad.qna.web.domain.UserRepository;
import com.codessquad.qna.web.exceptions.auth.UnauthorizedAccessException;
import com.codessquad.qna.web.exceptions.users.UserNotFoundException;
import com.codessquad.qna.web.utils.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UsersController {
    private final UserRepository userRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public String createUser(User createdUser) {
        userRepository.save(createdUser);
        LOGGER.info("user created : {}", createdUser.getUserId());
        return "redirect:/users";
    }

    @GetMapping
    public String getUserList(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "user/list";
    }

    @GetMapping("/{userId}")
    public String getOneUser(@PathVariable("userId") long id, Model model) {
        User foundUser = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        model.addAttribute("foundUser", foundUser);
        return "user/profile";
    }

    @GetMapping("/modify-form")
    public String getModifyUserPage() {
        return "user/modify-form";
    }

    @PutMapping("/{id}")
    public String modifyUser(@PathVariable long id, String prevPassword, String newPassword,
                             String name, String email, HttpSession session) {
        User loginUser = SessionUtil.getLoginUser(session);
        User foundUser = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        if(!loginUser.isMatchingId(foundUser)){
            throw new UnauthorizedAccessException("타인의 개인정보를 수정할 수 없습니다");
        }
        verifyAuthorizedAccess(loginUser, prevPassword);
        loginUser.update(newPassword, name, email);
        userRepository.save(loginUser);
        return "redirect:/users/" + loginUser.getId();
    }

    @PostMapping("/login")
    public String processLogin(String userId, String password, HttpSession session) {
        User foundUser = null;
        try {
            foundUser = userRepository.findByUserId(userId)
                    .orElseThrow(UserNotFoundException::new);
            verifyAuthorizedAccess(foundUser, password);
        } catch (RuntimeException e) {
            return "redirect:/users/login-form";
        }
        SessionUtil.setLoginUser(session, foundUser);
        LOGGER.info("user login : {}", foundUser.getUserId());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String processLogout(HttpSession session) {
        User sessionUser = SessionUtil.getLoginUser(session);
        LOGGER.info("user logout : {}", sessionUser.getUserId());
        SessionUtil.removeLoginUser(session);
        return "redirect:/";
    }

    private void verifyAuthorizedAccess(User user, String password) {
        if (!user.isMatchingPassword(password)) {
            throw new UnauthorizedAccessException("패스워드가 일치하지 않습니다");
        }
    }
}
