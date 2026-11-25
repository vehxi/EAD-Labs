package ru.kafpin.lab2.controller;

import jakarta.servlet.RequestDispatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AccessDeniedController {
    @RequestMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied(
            @RequestAttribute(name = RequestDispatcher.FORWARD_SERVLET_PATH, required = false) String path,
            Model model
    ) {
        model.addAttribute("path", path);
        return "error/403";
    }
}
