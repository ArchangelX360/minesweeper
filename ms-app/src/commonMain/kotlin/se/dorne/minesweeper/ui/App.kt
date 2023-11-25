package se.dorne.minesweeper.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import se.dorne.minesweeper.gameengine.*
import kotlin.math.ceil

@Composable
fun App() {
    MaterialTheme {
        var board by remember { mutableStateOf<Board?>(null) }
        Column(Modifier.fillMaxWidth().fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Minesweeper", fontSize = TextUnit(5f, TextUnitType.Em))
            when (val b = board) {
                null -> {
                    MinesweeperInitializer { columns, rows, mines ->
                        board = Board.initialBoard(columns, rows, mines)
                    }
                }

                else -> {
                    when (val gameState = b.gameSate()) {
                        is GameState.Finished -> {
                            var showDialog by remember { mutableStateOf(true) }
                            if (showDialog) {
                                EndOfGameDialog(gameState.outcome, onDismiss = { showDialog = false }, onRestart = {
                                    board = null
                                })
                            }
                            MinesweeperBoard(b, true) { _, _ -> }
                        }

                        GameState.Ongoing -> {
                            MinesweeperBoard(b, false) { cell, action ->
                                board = b.play(action, cell.index)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EndOfGameDialog(outcome: Outcome, onDismiss: () -> Unit, onRestart: () -> Unit) {
    val dialogText = when (outcome) {
        Outcome.WON -> "You won!"
        Outcome.LOST -> "You lost!"
    }
    AlertDialog(title = {
        Text(text = "End of game")
    }, text = {
        Text(text = dialogText)
    }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = onRestart,
        ) {
            Text("Restart game")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss
        ) {
            Text("Dismiss")
        }
    })
}

@Composable
fun MinesweeperBoard(board: Board, showEverything: Boolean, onCellClick: (cell: Cell, action: Action) -> Unit) {
    var action by remember { mutableStateOf(Action.REVEAL) }
    Column(modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Mark/Unmark as mine")
            Switch(checked = action == Action.REVEAL, onCheckedChange = {
                action = if (it) Action.REVEAL else Action.MARK_AS_MINE
            })
            Text("Reveal")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(board.columns),
        ) {
            items(board.cells) { cell ->
                MinesweeperCell(cell, board, showEverything, action, onCellClick)
            }
        }
    }
}

@Composable
fun MinesweeperCell(
    cell: Cell, board: Board, showEverything: Boolean, action: Action, onCellClick: (cell: Cell, action: Action) -> Unit
) {
    Button(
        onClick = { onCellClick(cell, action) },
        enabled = !showEverything && !cell.state.alreadyRevealed(),
    ) {
        val state = if (showEverything) cell.reveal(board, safely = true).state else cell.state
        val value = when (state) {
            is CellState.MarkedAsMine -> if (showEverything && state.rightfullyMarkedAsMine()) "❗" else "❌"
            CellState.Untouched -> " "
            CellState.Empty -> " "
            is CellState.Numbered -> "${state.surroundingMineCount}"
            CellState.RevealedMine -> "\uD83D\uDCA5"
            CellState.UnrevealedMine -> if (showEverything) "\uD83D\uDCA3" else " "
        }
        Text(value)
    }
}

@Composable
fun MinesweeperInitializer(onValidation: (columns: Int, rows: Int, mines: Int) -> Unit) {
    val (numberOfColumns, setColumns) = remember { mutableStateOf(9f) }
    val (numberOfRows, setRows) = remember { mutableStateOf(9f) }
    val (numberOfMines, setMines) = remember { mutableStateOf(10f) }

    Column(Modifier.fillMaxWidth(0.5f)) {
        Text("Number of rows: $numberOfRows")
        ParameterSlider(numberOfRows, setRows)
        Text("Number of columns: $numberOfColumns")
        ParameterSlider(numberOfColumns, setColumns)
        Text("Number of mines: $numberOfMines")
        ParameterSlider(numberOfMines, setMines, (numberOfRows * numberOfColumns) - 1)
        Button(
            enabled = numberOfRows * numberOfColumns > numberOfMines,
            content = {
                Text("Start game")
            },
            onClick = {
                onValidation(numberOfColumns.toInt(), numberOfRows.toInt(), numberOfMines.toInt())
            },
        )
    }
}

@Composable
fun ParameterSlider(value: Float, set: (Float) -> Unit, max: Float = 250f) {
    Slider(
        value = value,
        valueRange = 1f..max,
        onValueChange = {
            set(ceil(it))
        },
    )
}
