package com.codessquad.qna.web.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserRepository userRepository;

    Logger logger = LoggerFactory.getLogger(UsersController.class);

    @PostMapping
    public String createUser(User createdUser) {
        userRepository.save(createdUser);
        logger.info("user created : " + createdUser.getUserId());
        return "redirect:/users";
    }

    @GetMapping()
    public String getUserList(Model model, HttpSession session) {
        model.addAttribute("users", userRepository.findAll());
        return "user/list";
    }

    @GetMapping("/{userId}")
    public String getOneUser(@PathVariable("userId") long id, Model model) {
        User foundUser = getUserById(id);
        model.addAttribute("foundUser", foundUser);
        return "user/profile";
    }

    @GetMapping("/modify")
    public String getModifyUserPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser == null) {
            return "redirect:/";
        }
        return "user/modify-form";
    }

    private User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PutMapping("/modify")
    public String modifyUser(String prevPassword, String newPassword,
                             String name, String email, HttpSession session) {
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser == null) {
            return "redirect:/";
        }
        if (sessionUser.isMatchingPassword(prevPassword)) {
            if (!prevPassword.equals(newPassword)) {
                sessionUser.setPassword(newPassword);
            }
            sessionUser.setName(name);
            sessionUser.setEmail(email);
            userRepository.save(sessionUser);
            return "redirect:/users/" + sessionUser.getId();
        }
        return "redirect:/users";
    }

    @PostMapping("/login")
    public String processLogin(String userId, String password, HttpSession session) {
        User foundUser = userRepository.findByUserId(userId);
        if (foundUser == null) {
            return "redirect:/users/loginForm";
        }
        if (!foundUser.isMatchingPassword(password)) {
            return "redirect:/users/loginForm";
        }
        session.setAttribute(User.SESSION_KEY_USER_OBJECT, foundUser);
        logger.info("user login : " + foundUser.getUserId());
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String processLogout(HttpSession session) {
        User sessionUser = (User) session.getAttribute(User.SESSION_KEY_USER_OBJECT);
        if (sessionUser != null) {
            logger.info("user logout : " + sessionUser.getUserId());
            session.removeAttribute(User.SESSION_KEY_USER_OBJECT);
        }
        return "redirect:/";
    }
}
