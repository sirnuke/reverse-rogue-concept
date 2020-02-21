package com.degrendel.reverserogue.zircon

import com.degrendel.reverserogue.agent.RogueSoarAgent
import com.degrendel.reverserogue.common.*
import com.degrendel.reverserogue.world.RogueWorld
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.application.DebugConfig
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.screen.Screen
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Application(private val lock: ReentrantLock, private val condition: Condition)
{
  companion object
  {
    private val L by logger()
  }

  val agent: SoarAgent = RogueSoarAgent()
  val world: World = RogueWorld()

  private val floorTile = Tile.defaultTile().withCharacter('.')
  private val blockedTile = Tile.defaultTile().withCharacter(' ')
  private val corridorTile = Tile.defaultTile().withCharacter('#')
  private val doorTile = Tile.defaultTile().withCharacter('+')
  private val rogueTile = Tile.defaultTile().withCharacter(0x263A.toChar())

  private val wallTiles = EnumMap<WallDirection, Tile>(WallDirection::class.java)

  fun launch(zirconDebug: Boolean, drawGrid: Boolean)
  {
    val level = world.generateLevel()

    val debugConfig = if (drawGrid)
      DebugConfig(displayGrid = true, displayCoordinates = true, displayFps = true)
    else
      DebugConfig(displayGrid = false, displayCoordinates = false, displayFps = true)

    wallTiles[WallDirection.NORTH_SOUTH] = Tile.defaultTile().withCharacter(0x2551.toChar())
    wallTiles[WallDirection.EAST_WEST] = Tile.defaultTile().withCharacter(0x2550.toChar())
    wallTiles[WallDirection.NORTH_EAST] = Tile.defaultTile().withCharacter(0x255A.toChar())
    wallTiles[WallDirection.EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2554.toChar())
    wallTiles[WallDirection.SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2557.toChar())
    wallTiles[WallDirection.WEST_NORTH] = Tile.defaultTile().withCharacter(0x255D.toChar())
    wallTiles[WallDirection.NORTH_EAST_SOUTH] = Tile.defaultTile().withCharacter(0x2560.toChar())
    wallTiles[WallDirection.EAST_SOUTH_WEST] = Tile.defaultTile().withCharacter(0x2566.toChar())
    wallTiles[WallDirection.SOUTH_WEST_NORTH] = Tile.defaultTile().withCharacter(0x2563.toChar())
    wallTiles[WallDirection.WEST_NORTH_EAST] = Tile.defaultTile().withCharacter(0x2569.toChar())
    wallTiles[WallDirection.ALL] = Tile.defaultTile().withCharacter(0x256C.toChar())

    val tileGrid = SwingApplications.startTileGrid(
        AppConfig.newBuilder()
            .withSize(Level.WIDTH, Level.HEIGHT)
            .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
            .withDebugConfig(debugConfig)
            .withDebugMode(zirconDebug)
            .build())
    val screen = Screen.create(tileGrid)
    screen.onShutdown { lock.withLock { condition.signal() } }
    screen.theme = ColorThemes.adriftInDreams()

    screen.display()

    level.forEach { square ->
      val tile = when (square.type)
      {
        SquareType.BLOCKED -> blockedTile
        SquareType.CORRIDOR -> corridorTile
        SquareType.FLOOR -> floorTile
        SquareType.WALL -> wallTiles.getValue(square.wallDirection)
        SquareType.DOOR -> doorTile
      }
      tileGrid.draw(tile, Position.create(square.x, square.y))
    }
  }
}