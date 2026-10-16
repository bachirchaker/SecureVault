/**
 * SecureVault Frontend Interaction Script
 * Enterprise Client-Side Logic, Password Generation, Strength Meter & Decryption Handlers
 */

document.addEventListener('DOMContentLoaded', () => {
    initSidebarToggle();
    initCopyButtons();
    initPasswordReveal();
    initPasswordStrengthMeter();
    initPasswordGenerator();
    initDeleteConfirmationModal();
});

// Toast notification helper
function showToast(message, type = 'info') {
    let toast = document.getElementById('sv-global-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'sv-global-toast';
        toast.className = 'sv-toast';
        document.body.appendChild(toast);
    }
    
    let icon = 'bi-info-circle text-info';
    if (type === 'success') icon = 'bi-check-circle-fill text-success';
    if (type === 'warning') icon = 'bi-exclamation-triangle-fill text-warning';
    if (type === 'danger') icon = 'bi-shield-x text-danger';

    toast.innerHTML = `<i class="bi ${icon} fs-5"></i><span>${message}</span>`;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3500);
}

// Copy to clipboard
function initCopyButtons() {
    document.querySelectorAll('[data-copy-target]').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const targetId = btn.getAttribute('data-copy-target');
            const targetEl = document.getElementById(targetId);
            if (targetEl) {
                const textToCopy = targetEl.value !== undefined ? targetEl.value : targetEl.innerText;
                try {
                    await navigator.clipboard.writeText(textToCopy);
                    showToast('Copied to clipboard!', 'success');
                } catch (err) {
                    showToast('Failed to copy to clipboard', 'danger');
                }
            }
        });
    });

    document.querySelectorAll('[data-copy-text]').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.preventDefault();
            const textToCopy = btn.getAttribute('data-copy-text');
            if (textToCopy) {
                try {
                    await navigator.clipboard.writeText(textToCopy);
                    showToast('Copied to clipboard!', 'success');
                } catch (err) {
                    showToast('Failed to copy', 'danger');
                }
            }
        });
    });
}

// Reveal password via AJAX with CSRF
function initPasswordReveal() {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    document.querySelectorAll('.btn-reveal-pwd').forEach(btn => {
        btn.addEventListener('click', async () => {
            const entryId = btn.getAttribute('data-entry-id');
            const pwdDisplay = document.getElementById(`pwd-display-${entryId}`);
            const icon = btn.querySelector('i');

            if (!pwdDisplay) return;

            // If already revealed, mask it back
            if (btn.getAttribute('data-revealed') === 'true') {
                pwdDisplay.innerText = '••••••••••••';
                btn.setAttribute('data-revealed', 'false');
                if (icon) {
                    icon.classList.remove('bi-eye-slash');
                    icon.classList.add('bi-eye');
                }
                btn.title = "Reveal Password";
                return;
            }

            // Fetch decrypted secret from server
            try {
                btn.disabled = true;
                const headers = { 'Content-Type': 'application/json' };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                const response = await fetch(`/api/vault/${entryId}/reveal`, {
                    method: 'POST',
                    headers: headers
                });

                const data = await response.json();
                if (data.success) {
                    pwdDisplay.innerText = data.password;
                    btn.setAttribute('data-revealed', 'true');
                    if (icon) {
                        icon.classList.remove('bi-eye');
                        icon.classList.add('bi-eye-slash');
                    }
                    btn.title = "Hide Password";

                    // Auto-mask after 25 seconds for security
                    setTimeout(() => {
                        if (btn.getAttribute('data-revealed') === 'true') {
                            pwdDisplay.innerText = '••••••••••••';
                            btn.setAttribute('data-revealed', 'false');
                            if (icon) {
                                icon.classList.remove('bi-eye-slash');
                                icon.classList.add('bi-eye');
                            }
                        }
                    }, 25000);
                } else {
                    showToast(data.error || 'Failed to decrypt secret', 'danger');
                }
            } catch (err) {
                showToast('Decryption request failed', 'danger');
            } finally {
                btn.disabled = false;
            }
        });
    });
}

// Real-time password strength meter
function initPasswordStrengthMeter() {
    const pwdInput = document.getElementById('vault-password-input');
    const strengthBar = document.getElementById('strength-bar');
    const strengthLabel = document.getElementById('strength-label');
    const strengthFeedback = document.getElementById('strength-feedback');

    if (!pwdInput || !strengthBar) return;

    let debounceTimer;
    pwdInput.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(async () => {
            const pwd = pwdInput.value;
            if (!pwd) {
                strengthBar.style.width = '0%';
                if (strengthLabel) strengthLabel.innerText = '';
                if (strengthFeedback) strengthFeedback.innerHTML = '';
                return;
            }

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

            try {
                const res = await fetch('/api/vault/check-strength', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify({ password: pwd })
                });
                const data = await res.json();

                const percent = Math.min(100, (data.score + 1) * 20);
                strengthBar.style.width = `${percent}%`;

                let color = '#ef4444';
                if (data.score === 1) color = '#f59e0b';
                if (data.score === 2) color = '#38bdf8';
                if (data.score === 3) color = '#3b82f6';
                if (data.score === 4) color = '#10b981';

                strengthBar.style.backgroundColor = color;
                if (strengthLabel) {
                    strengthLabel.innerText = `${data.label} (${data.entropyBits} bits entropy | Crack time: ${data.estimatedCrackTime})`;
                    strengthLabel.style.color = color;
                }

                if (strengthFeedback) {
                    let feedbackHtml = '';
                    if (data.warnings && data.warnings.length > 0) {
                        feedbackHtml += `<div class="text-danger small mt-1"><i class="bi bi-exclamation-circle me-1"></i>${data.warnings[0]}</div>`;
                    } else if (data.suggestions && data.suggestions.length > 0) {
                        feedbackHtml += `<div class="text-muted small mt-1"><i class="bi bi-lightbulb me-1"></i>${data.suggestions[0]}</div>`;
                    }
                    strengthFeedback.innerHTML = feedbackHtml;
                }
            } catch (e) {
                console.error(e);
            }
        }, 200);
    });
}

// Password Generator Modal & Controls
function initPasswordGenerator() {
    const lenSlider = document.getElementById('gen-length-slider');
    const lenVal = document.getElementById('gen-length-val');
    const genBtn = document.getElementById('btn-run-generate');
    const genResult = document.getElementById('gen-result-display');
    const applyBtn = document.getElementById('btn-apply-generated');
    const targetInput = document.getElementById('vault-password-input');

    if (lenSlider && lenVal) {
        lenSlider.addEventListener('input', () => {
            lenVal.innerText = lenSlider.value;
        });
    }

    if (genBtn) {
        genBtn.addEventListener('click', async () => {
            const length = lenSlider ? parseInt(lenSlider.value) : 16;
            const uppercase = document.getElementById('gen-opt-upper')?.checked ?? true;
            const lowercase = document.getElementById('gen-opt-lower')?.checked ?? true;
            const numbers = document.getElementById('gen-opt-numbers')?.checked ?? true;
            const symbols = document.getElementById('gen-opt-symbols')?.checked ?? true;
            const avoidAmbiguous = document.getElementById('gen-opt-ambiguous')?.checked ?? false;

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

            try {
                const res = await fetch('/api/vault/generate', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify({ length, uppercase, lowercase, numbers, symbols, avoidAmbiguous })
                });
                const data = await res.json();
                if (genResult) {
                    genResult.value = data.password;
                }
            } catch (err) {
                showToast('Failed to generate password', 'danger');
            }
        });
    }

    if (applyBtn && genResult && targetInput) {
        applyBtn.addEventListener('click', () => {
            if (genResult.value) {
                targetInput.value = genResult.value;
                targetInput.dispatchEvent(new Event('input'));
                showToast('Generated password applied to vault form', 'success');
                const modal = bootstrap.Modal.getInstance(document.getElementById('generatorModal'));
                if (modal) modal.hide();
            }
        });
    }
}

// Delete Confirmation Modal
function initDeleteConfirmationModal() {
    const deleteModal = document.getElementById('deleteConfirmModal');
    if (!deleteModal) return;

    deleteModal.addEventListener('show.bs.modal', (e) => {
        const triggerBtn = e.relatedTarget;
        const entryId = triggerBtn.getAttribute('data-entry-id');
        const entryTitle = triggerBtn.getAttribute('data-entry-title');

        const titleSpan = deleteModal.querySelector('#delete-target-title');
        const form = deleteModal.querySelector('#delete-form');

        if (titleSpan) titleSpan.innerText = entryTitle || 'this entry';
        if (form) form.action = `/vault/${entryId}/delete`;
    });
}

// Sidebar toggle for mobile
function initSidebarToggle() {
    const toggleBtn = document.getElementById('sidebar-toggle');
    const sidebar = document.querySelector('.sv-sidebar');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }
}
