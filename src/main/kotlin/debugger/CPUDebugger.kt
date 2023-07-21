package debugger

import cpu.CPUState
import mediator.Component
import mediator.Mediator

class CPUDebugger(override var bus: Mediator) : Component {

    val disassembledProgram: MutableMap<UShort, CPUState> = mutableMapOf()


}