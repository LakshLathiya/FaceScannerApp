package com.assignment.facescannerapp

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope


fun main() {
    print(removeStarts("abc***def****"))
    print(arePointsInStraightLine(listOf(-1 to -1, 0 to 0, 1 to 1)))
    loadDataWithCoroutineScope()
}

fun loadDataWithCoroutineScope() = runBlocking {
    try {
        supervisorScope {
            val user = async {
                getUser()
            }
            val post = async {
                getPost()
            }
            val comment = async {
                getComments()
            }
            val results = listOf(user, post, comment).map {
                runCatching { it.await() }
            }
            results.forEach {
                it.onFailure { println("Error: ${it.message}") }
            }
        }
    } catch (e: Exception) {
        println("Inside Parent Scope: Caught exception: ${e.message}")
    }
}


suspend fun getUser() {
    println("getUser: started")
    delay(500)
    println("getUser: ended")
}

suspend fun getPost() {
    println("getPost: started")
    delay(1000)
    throw Exception("Exception in getPost")
    println("getPost: end")
}

suspend fun getComments() {
    println("getComments: started")
    delay(2000)
    println("getComments: end")
}

fun removeStarts(input: String): String {
    val stack = StringBuilder()
    for (ch in input) {
        if (ch == '*') {
            if (stack.isNotEmpty()) {
                stack.deleteCharAt(stack.length - 1)
            }
        } else {
            stack.append(ch)
        }
    }
    return stack.toString()
}


fun arePointsInStraightLine(points: List<Pair<Int, Int>>): Boolean {
    if (points.size <= 2) return true
    val (x1, y1) = points[0]
    val (x2, y2) = points[1]

    val dx = x2 - x1
    val dy = y2 - y1

    for (i in 2 until points.size) {
        val (x, y) = points[i]
        if ((y - y1) * dx != (x - x1) * dy) return false
    }
    return true
}
