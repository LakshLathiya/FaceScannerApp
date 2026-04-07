package com.assignment.facescannerapp

fun main() {
    print(removeStarts("abc***def****"))
    print(arePointsInStraightLine(listOf(-1 to -1, 0 to 0, 1 to 1)))
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
