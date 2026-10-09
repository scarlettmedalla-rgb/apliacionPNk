package com.example.myapplication.util

import android.util.Patterns

object ValidationUtils {

    /**
     * Strict name validation:
     * - Must be at least 2 characters long.
     * - Must contain ONLY alphabetic characters (A-Z, a-z, Spanish accents ÁÉÍÓÚáéíóúñÑ) and single spaces between words.
     * - Absolutely CANNOT contain digits (e.g. 12345), symbols, or script code.
     */
    fun isValidName(name: String?): Boolean {
        if (name.isNullOrBlank() || name.trim().length < 2) return false
        val clean = name.trim()
        val regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+(?:\\s[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)*$".toRegex()
        return regex.matches(clean) && !containsScriptOrInjection(clean)
    }

    /**
     * Alias for backwards compatibility with existing name checks.
     */
    fun isOnlyLettersAndSpaces(text: String?): Boolean {
        return isValidName(text)
    }

    /**
     * Checks if input contains dangerous script tags, HTML injection, SQL injection patterns, or shell commands.
     */
    fun containsScriptOrInjection(input: String?): Boolean {
        if (input.isNullOrBlank()) return false
        val lower = input.lowercase()

        // HTML / Script tags & event handlers
        if (lower.contains("<script") || lower.contains("</script") ||
            lower.contains("javascript:") || lower.contains("vbscript:") ||
            lower.contains("onload=") || lower.contains("onerror=") ||
            lower.contains("onclick=") || lower.contains("<iframe") ||
            lower.contains("<embed") || lower.contains("<object") ||
            lower.contains("<style") || lower.contains("<img") ||
            lower.contains("<a ") || lower.contains("</a")
        ) {
            return true
        }

        // Angle brackets HTML/XML injection
        if (lower.contains("<") || lower.contains(">")) {
            return true
        }

        // SQL injection keywords & comments
        if (lower.contains("--") || lower.contains("/*") || lower.contains("*/") ||
            lower.contains("' or '") || lower.contains("\" or \"") ||
            lower.contains("1=1") || lower.contains("union select") ||
            lower.contains("drop table") || lower.contains("delete from") ||
            lower.contains("insert into") || lower.contains("update ")
        ) {
            return true
        }

        // Shell command execution characters
        if (lower.contains("|") || lower.contains("&") || lower.contains("`") || lower.contains("$")) {
            return true
        }

        return false
    }

    /**
     * Validates email format and ensures no script injection is present.
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim()
        return Patterns.EMAIL_ADDRESS.matcher(clean).matches() && !containsScriptOrInjection(clean)
    }

    /**
     * Validates whether input is either a valid email or a valid username (at least 2 chars, no script injection).
     */
    fun isValidUsernameOrEmail(input: String?): Boolean {
        if (input.isNullOrBlank()) return false
        val clean = input.trim()
        if (containsScriptOrInjection(clean)) return false
        if (Patterns.EMAIL_ADDRESS.matcher(clean).matches()) return true
        return clean.length >= 2 && !clean.contains(" ")
    }

    /**
     * Validates password strength and ensures no script/injection attempt.
     */
    fun isRobustPassword(password: String?): Boolean {
        if (password.isNullOrEmpty() || password.length < 8) return false
        if (containsScriptOrInjection(password)) return false

        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasUpper && hasLower && hasDigit && hasSpecial
    }
}
