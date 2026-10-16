package com.securevault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.securevault.dto.VaultEntryExportDto;
import com.securevault.dto.VaultEntryRequestDto;
import com.securevault.dto.VaultEntryResponseDto;
import com.securevault.entity.Category;
import com.securevault.security.SecurityUtils;
import com.securevault.service.VaultService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public String listEntries(@RequestParam(value = "category", required = false) Category category,
                              @RequestParam(value = "q", required = false) String search,
                              @RequestParam(value = "favorites", required = false) Boolean favorites,
                              @RequestParam(value = "sort", required = false, defaultValue = "created") String sort,
                              Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<VaultEntryResponseDto> entries = vaultService.getEntries(userId, category, search, favorites, sort);

        model.addAttribute("entries", entries);
        model.addAttribute("categories", Category.values());
        model.addAttribute("currentCategory", category);
        model.addAttribute("searchQuery", search);
        model.addAttribute("favoritesOnly", favorites != null && favorites);
        model.addAttribute("currentSort", sort);

        return "vault/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("entry")) {
            VaultEntryRequestDto dto = new VaultEntryRequestDto();
            dto.setCategory(Category.LOGIN);
            model.addAttribute("entry", dto);
        }
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", false);
        return "vault/form";
    }

    @PostMapping("/new")
    public String createEntry(@Valid @ModelAttribute("entry") VaultEntryRequestDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "required", "Password is required for new entries");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("isEdit", false);
            return "vault/form";
        }

        Long userId = SecurityUtils.getCurrentUserId();
        vaultService.createEntry(userId, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Password entry created successfully!");
        return "redirect:/vault";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        VaultEntryResponseDto existing = vaultService.getEntryById(userId, id);

        VaultEntryRequestDto dto = new VaultEntryRequestDto();
        dto.setId(existing.getId());
        dto.setTitle(existing.getTitle());
        dto.setUsername(existing.getUsername());
        dto.setWebsiteUrl(existing.getWebsiteUrl());
        dto.setCategory(existing.getCategory());
        dto.setNotes(existing.getNotes());
        dto.setFavorite(existing.isFavorite());

        model.addAttribute("entry", dto);
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("entryId", id);

        return "vault/form";
    }

    @PostMapping("/{id}/edit")
    public String updateEntry(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("entry") VaultEntryRequestDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("entryId", id);
            return "vault/form";
        }

        Long userId = SecurityUtils.getCurrentUserId();
        vaultService.updateEntry(userId, id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Password entry updated successfully!");
        return "redirect:/vault";
    }

    @PostMapping("/{id}/delete")
    public String deleteEntry(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        vaultService.deleteEntry(userId, id);
        redirectAttributes.addFlashAttribute("successMessage", "Password entry deleted successfully.");
        return "redirect:/vault";
    }

    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable("id") Long id,
                                 @RequestHeader(value = "Referer", required = false) String referer) {
        Long userId = SecurityUtils.getCurrentUserId();
        vaultService.toggleFavorite(userId, id);
        return "redirect:" + (referer != null ? referer : "/vault");
    }

    @GetMapping("/export")
    public void exportVault(@RequestParam(value = "includePasswords", required = false, defaultValue = "false") boolean includePasswords,
                            @RequestParam(value = "format", required = false, defaultValue = "json") String format,
                            HttpServletResponse response) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        List<VaultEntryExportDto> exportData = vaultService.exportVault(userId, includePasswords);

        String filename = "securevault-export-" + System.currentTimeMillis();

        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".csv\"");

            StringBuilder csv = new StringBuilder();
            csv.append("Title,Username,Website,Category,Created,Password\n");
            for (VaultEntryExportDto item : exportData) {
                csv.append(escapeCsv(item.getTitle())).append(",")
                   .append(escapeCsv(item.getUsername())).append(",")
                   .append(escapeCsv(item.getWebsiteUrl())).append(",")
                   .append(escapeCsv(item.getCategory())).append(",")
                   .append(escapeCsv(item.getCreatedAt() != null ? item.getCreatedAt().toString() : "")).append(",")
                   .append(escapeCsv(item.getPassword() != null ? item.getPassword() : "[EXCLUDED]"))
                   .append("\n");
            }
            response.getWriter().write(csv.toString());
        } else {
            response.setContentType("application/json; charset=UTF-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".json\"");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(response.getOutputStream(), exportData);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
