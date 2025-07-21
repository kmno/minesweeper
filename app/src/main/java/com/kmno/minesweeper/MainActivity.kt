package com.kmno.minesweeper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmno.minesweeper.ui.theme.MinesweeperTheme

// --- Data Classes for Game State ---
data class Cell(
    val row: Int,
    val col: Int,
    var isCovered: Boolean = true,
    var isFlagged: Boolean = false,
    val isMine: Boolean = false, // We'll always know if it's a mine, even if covered
    var mineCountAround: Int = 0 // Number of mines in adjacent cells
)

// --- Colors for the game board (you can customize these) ---
val CoveredCellColor = Color(0xFFC0C0C0) // Standard grey for covered cells
val UncoveredCellColor = Color(0xFFE0E0E0) // Lighter grey for uncovered cells
val MineColor = Color.Red // For mines (when revealed)
val FlagColor = Color(0xFFFFCC00) // Yellow/Orange for flags
val BorderColor = Color(0xFF808080) // Darker grey for cell borders

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinesweeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MinesweeperGameScreen()
                }
            }
        }
    }
}

fun generateMinesweeperBoard(numRows: Int, numCols: Int, numMines: Int): List<Cell> {
    val cells = mutableListOf<Cell>()
    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            cells.add(Cell(row = row, col = col))
        }
    }
    cells.indices.shuffled().take(numMines).forEach { index ->
        cells[index] = cells[index].copy(isMine = true)
    }

    cells.forEachIndexed { index, cell ->
        val row = cell.row
        val col = cell.col
        Log.d("minesweeper", "row: $row, col: $col")
        if (!cell.isMine) {
            for (r in row - 1..row + 1) {
                for (c in col - 1..col + 1) {
                    if (r >= 0 && r < numRows && c >= 0 && c < numCols) {
                        if (cells.first() { it.row == r && it.col == c }.isMine) {
                            cell.mineCountAround++
                        }
                    }
                }
            }
        }
    }

    return cells
}

@Composable
fun MinesweeperGameScreen() {
    // This composable will hold our game state and logic
    // and pass them down to the Board composable.
    // Initial setup for a 8x8 board with 10 mines (dummy data for now)
    val numRows = 8
    val numCols = 8
    val numMines = 10

    val cells = remember {
        // Placeholder for initial cells. You'll generate the actual board here in Stage 1.
        val generatedBoard = generateMinesweeperBoard(numRows, numCols, numMines)
        mutableStateListOf<Cell>().apply { addAll(generatedBoard) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // TODO: Placeholder for game status (e.g., "Mines: 10", "Game Over", "You Win!")
        Text(
            text = "Minesweeper",
            fontSize = 24.sp,
            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
        )
        Spacer(modifier = Modifier.height(16.dp))

        MinesweeperBoard(
            cells = cells,
            onCellClick = { clickedCell ->
                println("Cell clicked: (${clickedCell.row}, ${clickedCell.col}")
                val index =
                    cells.indexOfFirst { it.row == clickedCell.row && it.col == clickedCell.col }
                if (index != -1) {
                    val currentCellInList = cells[index] // Get the up-to-date cell from the list
                    if (currentCellInList.isCovered || !currentCellInList.isFlagged) {
                        cells[index] = currentCellInList.copy(isCovered = false)
                        if (currentCellInList.isMine) {
                            // Handle game over
                        }
                    }
                }
            },
            onCellLongClick = { longClickedCell ->
                println("Cell long clicked: (${longClickedCell.row}, ${longClickedCell.col})")
                val index =
                    cells.indexOfFirst { it.row == longClickedCell.row && it.col == longClickedCell.col }
                if (index != -1) {
                    val currentCellInList = cells[index] // Get the up-to-date cell from the list
                    if (currentCellInList.isCovered) {
                        cells[index] = currentCellInList.copy(isFlagged = !currentCellInList.isFlagged)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinesweeperBoard(
    cells: List<Cell>,
    onCellClick: (Cell) -> Unit,
    onCellLongClick: (Cell) -> Unit
) {
    val numRows = 8 // Hardcoded for now, will derive from cells list later
    val numCols = 8 // Hardcoded for now, will derive from cells list later

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the board square
            .border(2.dp, BorderColor) // Outer border for the whole board
    ) {
        repeat(numRows) { row ->
            Row(modifier = Modifier.weight(1f)) {
                repeat(numCols) { col ->
                    val cell =
                        cells.first { it.row == row && it.col == col } // Find the correct cell
                    CellItem(
                        cell = cell,
                        onCellClick = onCellClick,
                        onCellLongClick = onCellLongClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.CellItem(
    cell: Cell,
    onCellClick: (Cell) -> Unit,
    onCellLongClick: (Cell) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f) // Each cell takes equal space in the row
            .fillMaxHeight()
            .border(1.dp, BorderColor) // Inner borders for cells
            .background(
                when {
                    cell.isFlagged -> FlagColor
                    cell.isCovered -> CoveredCellColor
                    else -> UncoveredCellColor // Uncovered
                }
            )
            .combinedClickable( // Handles both single click and long click
                onClick = { onCellClick(cell) },
                onLongClick = { onCellLongClick(cell) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell.isFlagged) {
            Text(text = "🚩", fontSize = 20.sp) // Flag emoji
        } else if (!cell.isCovered) {
            if (cell.isMine) {
                Text(text = "💣", fontSize = 20.sp) // Mine emoji
            } else if (cell.mineCountAround > 0) { // Only display count if > 0
                Text(text = cell.mineCountAround.toString(), fontSize = 16.sp, color = Color.Black)
            }
            // If mineCountAround is 0, we display nothing for an empty uncovered cell
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MinesweeperTheme {
        MinesweeperGameScreen()
    }
}