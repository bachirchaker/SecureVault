package com.securevault.controller;

import com.securevault.dto.ChangePasswordDto;
import com.securevault.dto.UserProfileDto;
import com.securevault.security.SecurityUtils;
import com.securevault.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserProfileDto profile = userService.getProfile(userId);

        model.addAttribute("profile", profile);
        if (!model.containsAttribute("changePassword")) {
            model.addAttribute("changePassword", new ChangePasswordDto());
        }
        return "profile/index";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePassword") ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (bindingResult.hasErrors()) {
            UserProfileDto profile = userService.getProfile(userId);
            model.addAttribute("profile", profile);
            return "profile/index";
        }

        try {
            userService.changePassword(userId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Master password successfully updated.");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            UserProfileDto profile = userService.getProfile(userId);
            model.addAttribute("profile", profile);
            model.addAttribute("errorMessage", e.getMessage());
            return "profile/index";
        }
    }
}
