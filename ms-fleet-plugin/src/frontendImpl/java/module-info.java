module se.dorne.minesweeper.fleet {
    requires fleet.kernel.plugins;
    requires fleet.noria.ui;
    requires fleet.frontend;
    requires se.dorne.minesweeper.gameengine;

    exports se.dorne.minesweeper.fleet;
    provides fleet.kernel.plugins.Plugin with se.dorne.minesweeper.fleet.MinesweeperPlugin;
}