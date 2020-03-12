package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T>{
    var out = this.toMutableList()
    while (!predicate(out.last())){
        out = out.dropLast(1).toMutableList()
    }
    out = out.dropLast(1).toMutableList()
    return out
}