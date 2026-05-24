package com.y9vad9.minesweeper.logic

fun Board.threeBV(): Int {
    if (rows == 0 || cols == 0) return 0

    val autoRevealed = Array(rows) { BooleanArray(cols) }

    var openings = 0
    val visited = Array(rows) { BooleanArray(cols) }

    val queue = ArrayDeque<Position>()

    for (r in 0 until rows) for (c in 0 until cols) {
        val start = Position(r, c)
        if (visited[r][c]) continue
        if (isMine(start)) continue
        if (adjacentMineCount(start) != 0) continue

        openings++
        visited[r][c] = true
        queue.addLast(start)

        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            autoRevealed[cur.row][cur.col] = true
            forEachNeighbor8(cur) { n ->
                autoRevealed[n.row][n.col] = true
                if (!visited[n.row][n.col] && !isMine(n) && adjacentMineCount(n) == 0) {
                    visited[n.row][n.col] = true
                    queue.addLast(n)
                }
            }
        }
    }

    var lonelyClicks = 0
    for (r in 0 until rows) for (c in 0 until cols) {
        val pos = Position(r, c)
        if (!isMine(pos) && !autoRevealed[r][c]) lonelyClicks++
    }

    return openings + lonelyClicks
}

private inline fun Board.forEachNeighbor8(pos: Position, action: (Position) -> Unit) {
    for (dr in -1..1) for (dc in -1..1) {
        if (dr == 0 && dc == 0) continue
        val r = pos.row + dr
        val c = pos.col + dc
        if (r in 0 until rows && c in 0 until cols) action(Position(r, c))
    }
}
