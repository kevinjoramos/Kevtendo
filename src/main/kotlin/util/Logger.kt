package util

import androidx.compose.ui.text.toUpperCase
import mediator.Component
import mediator.Mediator
import java.io.File

object Logger {
    private val logs = mutableListOf<String>()

    fun addLog(
        programCounter: UShort,
        opcode: UByte,
        operandLowByte: UByte?,
        operandHighByte: UByte?,
        opcodeName: String,
        targetAddress: UShort?,
        accumulatorValue: UByte,
        xRegisterValue: UByte,
        yRegisterValue: UByte,
        statusRegisterValue: UByte,
        stackPointerValue: UByte,
    ) {
        val pc = programCounter.to4DigitHexString().uppercase()
        val op = programCounter.to2DigitHexString().uppercase()
        val low = programCounter.to2DigitHexString().uppercase()
        val high = programCounter.to2DigitHexString().uppercase()


        //C000  4C F5 C5  JMP $C5F5                       A:00 X:00 Y:00 P:24 SP:FD PPU:  0, 21 CYC:7
        val logState = pc + "  " + op + "  " +
                (operandLowByte?.to2DigitHexString() ?: "  ") + "  " + (operandHighByte?.to2DigitHexString() ?: "  ") +
                "  " + opcodeName + "  " + accumulatorValue.to2DigitHexString() + " " +
                xRegisterValue.to2DigitHexString() + " " + yRegisterValue.to2DigitHexString() + " " +
                statusRegisterValue.to2DigitHexString() + " " + stackPointerValue.to2DigitHexString()

        println(logState)
        logs.add(logState)
    }

    fun writeLogsToFile() {
        File("logs.txt").printWriter().use { out ->
            logs.forEach {
                out.println(it)
            }
        }
    }
}