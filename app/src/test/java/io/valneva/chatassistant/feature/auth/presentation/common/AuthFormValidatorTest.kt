package io.valneva.chatassistant.feature.auth.presentation.common

import com.google.common.truth.Truth.assertThat
import io.valneva.chatassistant.R
import org.junit.Test

class AuthFormValidatorTest {

    @Test
    fun `validateEmail returns error for invalid email`() {
        val result = AuthFormValidator.validateEmail("invalid-email")

        assertThat(result).isEqualTo(R.string.auth_error_invalid_email)
    }

    @Test
    fun `validateEmail returns null for valid email`() {
        val result = AuthFormValidator.validateEmail("user@example.com")

        assertThat(result).isNull()
    }

    @Test
    fun `validatePassword returns short password error`() {
        val result = AuthFormValidator.validatePassword("12345")

        assertThat(result).isEqualTo(R.string.auth_error_short_password)
    }

    @Test
    fun `validateConfirmPassword returns mismatch error`() {
        val result = AuthFormValidator.validateConfirmPassword(
            password = "123456",
            confirmPassword = "654321",
        )

        assertThat(result).isEqualTo(R.string.auth_error_passwords_mismatch)
    }

    @Test
    fun `validateConfirmPassword returns null for matching passwords`() {
        val result = AuthFormValidator.validateConfirmPassword(
            password = "123456",
            confirmPassword = "123456",
        )

        assertThat(result).isNull()
    }
}
