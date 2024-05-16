package com.rm.loginappcompose.googlesignin

import android.util.Log
import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT

private const val TAG = "jwt"

data class GoogleUser(
    val sub: String?,
    val email: String?,
    val emailVerified: Boolean?,
    val fullName: String?,
    val givenName: String?,
    val familyName: String?,
    val picture: String?,
    val issuedAt: Long?,
    val expirationTime: Long?,
    val locale: String?
)

object Claims {
    const val SUB = "sub"
    const val EMAIL = "email"
    const val EMAIL_VERIFIED = "email_verified"
    const val FUll_NAME = "name"
    const val GIVEN_NAME = "given_name"
    const val FAMILY_NAME = "family_name"
    const val PICTURE = "picture"
    const val ISSUED_AT = "iat"
    const val EXPIRATION_TIME = "exp"
    const val LOCALE = "locale"
}

fun getUserFromTokenId(tokenId: String): GoogleUser? {
   try {
        val jwt = JWT(tokenId)
        return GoogleUser(
            sub = jwt.claims[Claims.SUB]?.asString(),
            email = jwt.claims[Claims.EMAIL]?.asString(),
            emailVerified = jwt.claims[Claims.EMAIL_VERIFIED]?.asBoolean(),
            fullName = jwt.claims[Claims.FUll_NAME]?.asString(),
            givenName = jwt.claims[Claims.GIVEN_NAME]?.asString(),
            familyName = jwt.claims[Claims.FAMILY_NAME]?.asString(),
            picture = jwt.claims[Claims.PICTURE]?.asString(),
            issuedAt = jwt.claims[Claims.ISSUED_AT]?.asLong(),
            expirationTime = jwt.claims[Claims.EXPIRATION_TIME]?.asLong(),
            locale = jwt.claims[Claims.LOCALE]?.asString()
        )
    } catch (e: DecodeException) {
        Log.e(TAG, e.toString())
        return null
    } catch (e: Exception) {
        Log.e(TAG, e.toString())
        return null
    }
}

