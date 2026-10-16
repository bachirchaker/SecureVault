package com.securevault.controller;

import com.securevault.dto.SecurityEventDto;
import com.securevault.security.SecurityUtils;
import com.securevault.service.SecurityAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/audit")
public class AuditController {

    private final SecurityAuditService auditService;

    public AuditController(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public String viewAuditLogs(@RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "15") int size,
                                Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(50, size));
        Page<SecurityEventDto> eventPage = auditService.getEvents(userId, pageable);

        model.addAttribute("events", eventPage.getContent());
        model.addAttribute("currentPage", eventPage.getNumber());
        model.addAttribute("totalPages", eventPage.getTotalPages());
        model.addAttribute("totalElements", eventPage.getTotalElements());

        return "audit/logs";
    }
}
