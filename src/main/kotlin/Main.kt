import Bus.Bus
import CPU.CPU6502
import Cartridge.Cartridge
import Cartridge.MapperZero
import PPU.PPU2C02
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@ExperimentalUnsignedTypes
fun mainSecond(args: Array<String>) {
    val projectRootPath = System.getProperty("user.dir")
    val pathSeparator = System.getProperty("file.separator")
    val pathToGame = "$projectRootPath/src/main/kotlin/games/Donkey Kong.nes"

    val cpu = CPU6502()
    val ppu = PPU2C02()
    val cartridge = Cartridge(pathToGame)
    val mapper = MapperZero(cartridge)
    Bus(cpu, UByteArray(2048), ppu, mapper)

    while (true) {
        cpu.run()
    }
}

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}



