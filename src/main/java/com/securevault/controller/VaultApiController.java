package com.securevault.controller;

import com.securevault.dto.PasswordGeneratorOptionsDto;
import com.securevault.dto.PasswordStrengthResultDto;
import com.securevault.dto.RevealPasswordResponseDto;
import com.securevault.security.SecurityUtils;
import com.securevault.service.PasswordGeneratorService;
import com.securevault.service.PasswordStrengthService;
import com.securevault.service.VaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultApiController {

    private final VaultService vaultService;
    private final PasswordGeneratorService generatorService;
    private final PasswordStrengthService strengthService;

    public VaultApiController(VaultService vaultService,
                              PasswordGeneratorService generatorService,
                              PasswordStrengthService strengthService) {
        this.vaultService = vaultService;
        this.generatorService = generatorService;
        this.strengthService = strengthService;
    }

    @PostMapping("/{id}/reveal")
    public ResponseEntity<RevealPasswordResponseDto> revealPassword(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            String plainPassword = vaultService.revealPassword(userId, id);
            return ResponseEntity.ok(RevealPasswordResponseDto.ofSuccess(id, plainPassword));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(RevealPasswordResponseDto.ofError(e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePassword(@RequestBody(required = false) PasswordGeneratorOptionsDto options) {
        if (options == null) {
            options = new PasswordGeneratorOptionsDto();
        }
        String generated = generatorService.generatePassword(options);
        PasswordStrengthResultDto strength = strengthService.evaluateStrength(generated);

        return ResponseEntity.ok(Map.of(
                "password", generated,
                "strength", strength
        ));
    }

    @PostMapping("/check-strength")
    public ResponseEntity<PasswordStrengthResultDto> checkStrength(@RequestBody Map<String, String> payload) {
        String password = payload.getOrDefault("password", "");
        PasswordStrengthResultDto result = strengthService.evaluateStrength(password);
        return ResponseEntity.ok(result);
    }
}
