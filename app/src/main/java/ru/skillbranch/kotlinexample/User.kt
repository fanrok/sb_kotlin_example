package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null
    val userInfo: String
    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()
    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")
    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")
        }
    private var _login: String? = null
    var login: String
        set(value) {
            _login = value.toLowerCase()
        }
        get() = _login!!
    private lateinit var passwordHash: String
    private var salt: String = generateSalt()

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary email constructor called")
        passwordHash = encrypt(password)

    }

    constructor(
        firstName: String,
        lastName: String?,
        phone: String
    ) : this(firstName, lastName, rawPhone = phone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor called")
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(phone, code)
    }

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        phone: String?,
        salt: String,
        hash: String
    ) : this(firstName=firstName, lastName=lastName, rawPhone = phone, email = email, meta = mapOf("auth" to "csv")) {
        println("Secondary csv constructor called")
        this.salt = salt
        this.passwordHash = hash

    }

    init {
        println("Init")
        check(!firstName.isBlank()) { "First Name mast be not blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "Email or phone mast be not blank" }
        phone = rawPhone
        login = email ?: phone!!
        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun generateSalt():String{
        return ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
    }

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash
    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The entered password does not match the current password")
    }

    private fun sendAccessCodeToUser(phone: String, code: String) {
        println("......... snding code $code on $phone")
    }

    fun changeAccessCode(){
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(phone!!, code)
    }

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefjhijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    private fun encrypt(password: String): String = salt.plus(password).md5()

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return when{
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        fun csvUser(
            fullName: String,
            email: String?,
            phone: String?,
            salt: String,
            hash: String
        ): User {
            val (firstName, lastName) = fullName.fullNameToPair()

            return when{
                !phone.isNullOrBlank() || !email.isNullOrBlank() -> User(
                    firstName=firstName,
                    lastName=lastName,
                    email=email,
                    phone=phone,
                    salt=salt,
                    hash=hash)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }


        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("Fullmane must contain only first name and last name? current split result: ${this@fullNameToPair}")
                    }
                }
        }
    }

}