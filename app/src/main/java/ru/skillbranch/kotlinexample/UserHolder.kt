package ru.skillbranch.kotlinexample

object UserHolder {
    private var map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User {
//        println(map[email]?.userInfo)
        if (!map.containsKey(email.toLowerCase())) {
            return User.makeUser(fullName = fullName, email = email, password = password)
                .also { user ->
                    map[user.login] = user
                }
        } else {
            throw IllegalArgumentException("A user with this email already exists")
        }
    }

    fun loginUser(login: String, password: String): String? {
        val phone2 = login.trim().replace("[^+\\d]".toRegex(), "")
        if (phone2.length == 12) {
            return map[phone2]?.run {
                if (checkPassword((password))) this.userInfo
                else null
            }
        } else {
            return map[login.trim().toLowerCase()]?.run {
                if (checkPassword((password))) this.userInfo
                else null
            }
        }
    }

    fun clearHolder() {
        map.clear()
    }

    fun registerUserByPhone(fullName: String, phone: String): User {
        val phone2 = phone.replace("[^+\\d]".toRegex(), "")
        if (!map.containsKey(phone2)) {
            if (phone2.length == 12) {
                return User.makeUser(fullName = fullName, phone = phone)
                    .also { user ->
                        map[user.login] = user
                    }
            } else {
                throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
            }
        } else {
            throw IllegalArgumentException("A user with this phone already exists")
        }
    }

    fun requestAccessCode(login: String): Unit {
        val phone2 = login.replace("[^+\\d]".toRegex(), "")
        map[phone2]?.changeAccessCode()
    }

    fun importUsers(list: List<String>): List<User> {
        val out = mutableListOf<User>()
        list.onEach {
            val array = it.split(";")
            //Полное имя пользователя; email; соль:хеш пароля; телефон
            val fullName = array[0]
//            val email = array[1]
            var email:String? = null
            if(!array[1].isBlank()){
                email = array[1]
            }
            val arraySH = array[2].split(":")
            val salt = arraySH[0]
            val hash = arraySH[1]
            var phone:String? = null
            if(!array[3].isBlank()){
                phone = array[3]
            }
            if(email!=null || phone!=null) {
                val user = User.csvUser(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        salt = salt,
                        hash = hash
                    )
                    .also { user ->
                        map[user.login] = user
                    }
                out.add(user)
            }
        }
        return out
    }
}