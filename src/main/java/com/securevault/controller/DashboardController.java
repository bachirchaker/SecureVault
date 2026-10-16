package com.securevault.controller;

import com.securevault.dto.SecurityStatsDto;
import com.securevault.security.SecurityUtils;
import com.securevault.service.VaultService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final VaultService vaultService;

    public DashboardController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public String dashboard(Model model) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return "redirect:/login";
        }

        SecurityStatsDto stats = vaultService.getSecurityDashboardStats(currentUserId);
        model.addAttribute("stats", stats);
        model.addAttribute("username", SecurityUtils.getCurrentUsername());

        return "dashboard/index";
    }
}
