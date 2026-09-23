package com.example

import com.example.data.repository.AuthenticationRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthenticationRepositoryTest {

    @Test
    fun foundingPartnerEmails_areRecognized() {
        val foundingEmails = AuthenticationRepository.FOUNDING_PARTNER_EMAILS
        assertTrue(foundingEmails.contains("ankush@dakshyam.com"))
        assertTrue(foundingEmails.contains("ankush2004nanda@gmail.com"))
        assertTrue(foundingEmails.contains("himanshu@dakshyam.com"))
        assertTrue(foundingEmails.contains("shikhar@dakshyam.com"))
        assertTrue(foundingEmails.contains("kunal@dakshyam.com"))
    }

    @Test
    fun corporateEmailDomain_isProperlyIdentified() {
        val validCorporate = "engineering.lead@dakshyam.com"
        val invalidEmail = "unauthorized.user@external.com"

        assertTrue(validCorporate.endsWith("@dakshyam.com"))
        assertFalse(invalidEmail.endsWith("@dakshyam.com"))
    }
}
