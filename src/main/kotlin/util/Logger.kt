package util

import Bus.Bus
import androidx.compose.ui.text.toUpperCase
import mediator.Component
import mediator.Mediator
import java.io.File

object Logger {
    private val projectRootPath = System.getProperty("user.dir")
    private val logs = mutableListOf<String>()
    private val pathToLog = "$projectRootPath/logs/logs.txt"

    fun addLog(
        programCounter: UShort,
        opcode: UByte,
        operandLowByte: UByte?,
        operandHighByte: UByte?,
        opcodeName: String,
        targetAddress: UShort?,
        immediateOperand: UByte?,
        accumulatorValue: UByte,
        xRegisterValue: UByte,
        yRegisterValue: UByte,
        statusRegisterValue: UByte,
        stackPointerValue: UShort
    ) {
        val pc = programCounter.to4DigitHexString().uppercase()
        val op = opcode.to2DigitHexString().uppercase()
        val low = operandLowByte?.to2DigitHexString()?.uppercase() ?: "  "
        val high = operandHighByte?.to2DigitHexString()?.uppercase() ?: "  "
        val acc = "A:${accumulatorValue.to2DigitHexString().uppercase()}"
        val x = "X:${xRegisterValue.to2DigitHexString().uppercase()}"
        val y = "Y:${yRegisterValue.to2DigitHexString().uppercase()}"
        val p = "P:${statusRegisterValue.to2DigitHexString().uppercase()}"
        val sp = "SP:${stackPointerValue.toUByte().to2DigitHexString().uppercase()}"
        val address: String =
            if (immediateOperand != null) {
                String.format("%-27s","#$${immediateOperand.to2DigitHexString().uppercase()}")
            } else if (targetAddress != null) {
                String.format("%-27s", "$${targetAddress.to4DigitHexString().uppercase()}")
            } else
                String.format("%-27s", "")


        //C000  4C F5 C5  JMP $C5F5                       A:00 X:00 Y:00 P:24 SP:FD PPU:  0, 21 CYC:7
        val logState = "$pc  $op $low $high  $opcodeName $address $acc $x $y $p $sp"

        println(logState)
        logs.add(logState)
    }

    fun writeLogsToFile() {
        File(pathToLog).printWriter().use { out ->
            logs.forEach {
                out.println(it)
            }
        }
    }
}