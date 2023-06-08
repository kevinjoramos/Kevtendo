package cpu

enum class AddressingMode {
    IMP,
    A,
    IMM,
    ABS,
    ABS_X,
    ABS_Y,
    ZPG,
    ZPG_X,
    ZPG_Y,
    IND,
    X_IND,
    IND_Y,
    REL
}