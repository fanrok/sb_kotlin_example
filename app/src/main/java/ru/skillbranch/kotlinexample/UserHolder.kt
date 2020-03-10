package ru.skillbranch.kotlinexample

object UserHolder {
    private var map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ):User{
        return User.makeUser(fullName, email=email, password=password)
            .also{
                user->map[user.login] = user
            }
    }

    fun loginUser(login: String, password: String):String?{
        return map[login.trim()]?.run{
            if(checkPassword((password))) this.userInfo
            else null
        }
    }

    fun clearHolder(){
        ///
    }

    fun registerUserByPhone(fullName: String, phone: String):User{
        return User.makeUser("1")
    }

    fun requestAccessCode(phone: String): String{
        return "123"
    }
}