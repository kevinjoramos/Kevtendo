package ui

import androidx.compose.runtime.*
import bus.Bus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mediator.Event
import mediator.Sender
import util.Logger
import util.to4DigitHexString
import kotlin.system.measureTimeMillis

@ExperimentalUnsignedTypes
class NesEmulatorUiState {
    private val projectRootPath = System.getProperty("user.dir")

    //private val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"
    //private val pathToGame = "$projectRootPath/src/main/kotlin/games/PacMan.nes"
    //private val pathToGame = "$projectRootPath/src/main/kotlin/games/nestest.nes"
    private val pathToGame = "$projectRootPath/src/main/kotlin/games/Super Mario Bros.nes"
    private var bus = Bus(pathToGame)

    private val _gameViewUiState = MutableStateFlow(GameViewUiState())
    val gameViewUiState = _gameViewUiState.asStateFlow()

    var zeroPageState = mutableStateOf(List<UByte>(256) { 0u })
    var mainRegistersState = mutableStateOf(List<UInt>(5) {0u} )
    var mainFlagsState = mutableStateOf(List<Boolean>(8) { false })
    var disassemblerState = mutableStateOf("$0000: 00")
    var patternTableState = mutableStateOf(listOf<UInt>())
    var paletteColorsState = mutableStateOf(List(32) { 0x0fu })
    var isHudVisible = mutableStateOf(true)
    private var isRunning = false
    var isPaused = false

    private var systemClock = 0


    /**
     * DMA
     */
    private var isDMAReady = false
    private var highByteDMAPointer = 0u
    private var lowByteDMAPointer = 0u
    private var currentDMAData = 0u

    private var emulatorProcess: Job? = null

    var controller1 = bus.controller1
    var controller2 = bus.controller2

    fun start() {
        isRunning = true
        GlobalScope.launch(Dispatchers.Default) {
            runSystem()
        }
    }

    fun step() {
        bus.cpu.run()


        //instructionSlidingWindowState = getCurrentInstructionsSlidingWindowState()
    }

    fun reset() {
        isRunning = false
        emulatorProcess?.cancel()
        this.bus = Bus(pathToGame)

        start()
    }

    fun stop() {
        isRunning = false
    }

    private suspend fun runSystem() {
        while (isRunning) {

            val elapsedTime = measureTimeMillis {
                executeMainCycle()
                updatePaletteTableState()
            }

            println(elapsedTime)

            delay(MILLISECONDS_PER_FRAME - elapsedTime)

            systemClock = 0
        }
    }

    private fun executeMainCycle() {

        while (systemClock < TOTAL_SYSTEM_CYCLES_PER_FRAME) {
            // execute one cycle of ppu (outputs one pixel)
            bus.ppu.run()

            // execute one cycle of cpu (1 cpu cycle for every 3 ppu cycles).
            // we also need to check for DMA suspends.
            if (systemClock % 3 == 0) {

                if (bus.cpu.isSuspendedForDMA) {

                    // When a dma suspend is pending we need to wait for a read cycle (for my emulation this is every even cycle).
                    if (systemClock.mod(2) == 0 && !isDMAReady) {

                        // Here we set up our values to intercept the cpu clock cycles.
                        isDMAReady = true
                        highByteDMAPointer = bus.ppu.dmaRegister
                        lowByteDMAPointer = 0u
                    }

                    // When we get the right cycle, we can start writing (odd) / reading (even).
                    if (isDMAReady) {

                        // Read
                        if (systemClock.mod(2) == 0) {
                            currentDMAData = bus.readAddress(((highByteDMAPointer shl 8) or lowByteDMAPointer).toUShort()).toUInt()
                        }

                        // Write
                        else {
                            bus.ppu.writeToOamDataRegister(currentDMAData)

                            // When we are done, go back to normal cpu activities.
                            if (lowByteDMAPointer == 0xFFu) {
                                bus.cpu.isSuspendedForDMA = false
                                isDMAReady = false
                                highByteDMAPointer = 0u
                                lowByteDMAPointer = 0u
                                currentDMAData = 0u
                            } else {
                                lowByteDMAPointer++
                            }
                        }
                    }
                }

                // Run normally
                else {
                    bus.cpu.run()

                    // update debugger.
                    if (bus.cpu.cycleCount == 0) {

                    }
                }
            }

            if (systemClock.mod(500) == 0) {
            }


            // At this point visible scan lines are complete.
            if ((systemClock == FIRST_CYCLE_AFTER_RENDER)) {
                //generateNoise()
                updateGameViewState()

                if (isHudVisible.value) {
                    updateDisassemblerState()
                    updateMainCpuState()
                    updateZeroPageState()
                }
            }

            systemClock++
        }
    }

    private fun updateGameViewState() {
        val pixelScreen = bus.ppu.frameBuffer.map { it.toList().toImmutableList() }.toImmutableList()
        _gameViewUiState.update {
            it.copy(pixelScreen = pixelScreen)
        }
    }

    private fun updateZeroPageState() {
        zeroPageState.value = bus.ram.memory.take(256).toImmutableList()
    }

    private fun updateMainCpuState() {
        mainRegistersState.value = listOf<UInt>(
            bus.cpu.programCounter.toUInt(),
            bus.cpu.accumulator.toUInt(),
            bus.cpu.xRegister.toUInt(),
            bus.cpu.yRegister.toUInt(),
            bus.cpu.stackPointer.toUInt()
        ).toImmutableList()

        mainFlagsState.value = listOf<Boolean>(
            bus.cpu.negativeFlag,
            bus.cpu.overflowFlag,
            bus.cpu.extraFlag,
            bus.cpu.breakFlag,
            bus.cpu.decimalFlag,
            bus.cpu.interruptDisableFlag,
            bus.cpu.zeroFlag,
            bus.cpu.carryFlag
        ).toImmutableList()
    }

    private fun updateDisassemblerState() {
        val currentIndex = bus.cpu.disassembler.instructionIndexMap[bus.cpu.programCounter]
        if (currentIndex == null) {
            disassemblerState.value = "$${bus.cpu.programCounter.to4DigitHexString()}: ???"
        } else {
            disassemblerState.value = bus.cpu.disassembler.instructionList[currentIndex]
        }
    }

    private fun updatePatternTableState() {
        val patternBuffer = mutableListOf<UInt>()
        val palettes = bus.ppu.paletteTable.map { it.toUInt() }
        val palette = bus.ppu.paletteTable.take(4).map { it.toUInt() }

        for (tileBitPlanes in bus.mapper.cartridge.characterRom.chunked(16)) {
            for (index in 0..7) {
                val lowTile = tileBitPlanes[index].toUInt()
                val highTile = tileBitPlanes[index + 8].toUInt()

                patternBuffer.add(((highTile and 0x80u) shr 6) or ((lowTile and 0x80u) shr 7))
                patternBuffer.add(((highTile and 0x40u) shr 5) or ((lowTile and 0x40u) shr 6))
                patternBuffer.add(((highTile and 0x20u) shr 4) or ((lowTile and 0x20u) shr 5))
                patternBuffer.add(((highTile and 0x10u) shr 3) or ((lowTile and 0x10u) shr 4))
                patternBuffer.add(((highTile and 0x08u) shr 2) or ((lowTile and 0x08u) shr 3))
                patternBuffer.add(((highTile and 0x04u) shr 1) or ((lowTile and 0x04u) shr 2))
                patternBuffer.add(((highTile and 0x02u) shr 0) or ((lowTile and 0x02u) shr 1))
                patternBuffer.add(((highTile and 0x01u) shl 1) or ((lowTile and 0x01u) shr 0))
            }
        }
        val paletteIndex = (bus.ppu.overridingPalette ?: 0u) shl 2
        patternTableState.value = patternBuffer.map { palettes[(paletteIndex + it).toInt()] }.toImmutableList()
    }

    fun updatePaletteTableState() {
        val palettes = bus.ppu.paletteTable.map { it.toUInt() }.toMutableList()
        for (i in 0..palettes.lastIndex step 4) {
            palettes[i] = palettes[0]
        }
        paletteColorsState.value = palettes.toImmutableList()
        updatePatternTableState()
    }

    fun forcePaletteSwap(paletteSelect: UInt) {

        if (bus.ppu.overridingPalette == paletteSelect) {
            bus.ppu.overridingPalette = null
        } else {
            bus.ppu.overridingPalette = paletteSelect
        }

    }

    /*private fun generateNoise() {
        val pixelScreen = List(240) {
            List(257) {
                if ((0..1).random() == 0) Color.Black else Color.White
            }.toImmutableList()
        }.toImmutableList()

        _gameViewUiState.update {
            it.copy(pixelScreen = pixelScreen)
        }
    }*/

    fun updateController() {

    }

    companion object {
        const val TOTAL_SYSTEM_CYCLES_PER_FRAME = 89_342
        const val FIRST_CYCLE_AFTER_RENDER = 81_841
        //const val FIRST_CYCLE_AFTER_RENDER = 82_080

        const val MILLISECONDS_PER_FRAME = 14
    }
}