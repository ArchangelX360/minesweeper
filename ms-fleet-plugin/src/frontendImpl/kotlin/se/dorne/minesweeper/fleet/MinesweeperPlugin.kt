package se.dorne.minesweeper.fleet

import fleet.frontend.actions.actions
import fleet.frontend.actions.sagaCallback
import fleet.frontend.layout.ToolPosition
import fleet.frontend.navigation.ViewTypeId
import fleet.frontend.navigation.viewType
import fleet.kernel.plugins.ContributionScope
import fleet.kernel.plugins.Plugin
import fleet.kernel.plugins.PluginScope
import fleet.util.UID
import kotlinx.serialization.Serializable
import noria.NoriaContext
import noria.ui.components.center
import noria.ui.components.constrain
import noria.ui.components.hbox
import noria.ui.components.vbox
import se.dorne.minesweeper.gameengine.Board

class MinesweeperPlugin : Plugin<Unit> {
    companion object : Plugin.Key<Unit>

    override val key: Plugin.Key<Unit> = MinesweeperPlugin

    override fun ContributionScope.load(pluginScope: PluginScope) {
        actions {
            action(id = MinesweeperActionIds.Minesweeper.id, "Open Minesweeper") {
                sagaCallback {

                }
            }
        }
    }
}

enum class MinesweeperActionIds(val id: String) {
    Minesweeper("open-minesweeper"),
}

@Serializable
sealed class MinesweeperNavigationTarget {
    companion object {
        val ViewType = ViewTypeId<MinesweeperNavigationTarget>("se.dorne.minesweeper")
    }

    @Serializable
    object NewGame : MinesweeperNavigationTarget()

    @Serializable
    data class GameInProgress(val id: UID, val board: Board) : MinesweeperNavigationTarget()
}

fun ContributionScope.minesweeperTab() {
    viewType(MinesweeperNavigationTarget.ViewType, MinesweeperNavigationTarget.serializer()) {
        defaultPosition = ToolPosition.MainPanel
        displayName = "Minesweeper"
        defaultLocation {
            DefaultLocation {
                MinesweeperNavigationTarget.NewGame
            }
        }
        tabKey { Any() }
        Open { target ->
            val size = querySettingsKey(TicTacToeSettingsKeys.boardSize)
            val entity = change {
                val (actualGame, tabPlayer) = shared {
                    when(target) {
                        is MinesweeperNavigationTarget.NewGame -> getOrCreateGameInstance(size)
                        is MinesweeperNavigationTarget.GameInProgress ->
                            loadGameInProgress(target.id, target.size, target.status, target.cells) to target.player
                    }
                }
                new(TicTacToeTab::class) {
                    game = actualGame
                    player = tabPlayer
                }
            }
            View(entity) {
                val game = entity.game
                header {
                    displayName =
                        "Tic-Tac-Toe (${entity.player}, ${game.shortenedID()}...)"
                }
                currentLocation {
                    if (game.status != GameStatus.OVER)
                        MinesweeperNavigationTarget.GameInProgress(
                            game.id,
                            game.board.size, game.status, game.cells(), entity.player)
                    else
                        MinesweeperNavigationTarget.NewGame
                }
                Renderer {
                    minesweeperBoard(entity)
                }
            }
        }
    }
}

fun NoriaContext.minesweeperBoard(board: Board, showEverything: Boolean) {
    val game = tab.game
    var cellSize = 0
    center {
        constrain({ cs ->
            val gridSize = min(cs.maxWidth, cs.maxHeight).toFloat() * 0.8f
            val size = gridSize.roundToInt()
            cellSize = (gridSize / game.board.size - 1f).roundToInt()
            cs.copy(maxWidth = size, maxHeight = size)
        }) {
            vbox {
                game.board.rows.forEach {
                    hbox {
                        it.map { it.deref() }.forEach { cell ->
                            constrain({ cs ->
                                cs.copy(maxWidth = cellSize, maxHeight = cellSize)
                            }) {
                                val pluginScope = PluginScopeKey.value
                                ticTacToeCell(cell, tab.isActive) {
                                    if (cell.shape.isEmpty() && tab.isActive) {
                                        pluginScope.changeAsync {
                                            shared {
                                                game.proceed(cell)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}