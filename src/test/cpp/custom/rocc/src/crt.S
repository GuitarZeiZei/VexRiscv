.globl _start
_start:

#define r_type_insn(_f7, _rs2, _rs1, _f3, _rd, _opc) \
.word (((_f7) << 25) | ((_rs2) << 20) | ((_rs1) << 15) | ((_f3) << 12) | ((_rd) << 7) | ((_opc) << 0))

#define RoCCFadd(_rd, _rs1, _rs2 ) \
r_type_insn(0b0000010, _rs2, _rs1, 0b110, _rd, 0b0001011)

li x28, 1
//0.2
li x1,0x3E4CCCCC
//0.4
li x2,0x3ECCCCCC

li x4,0x3F199999
RoCCFadd(3,1,2)
bne x3,x4,fail
j pass

fail: //x28 => error code
    li x2, 0xF00FFF24
    sw x28, 0(x2)

pass:
    li x2, 0xF00FFF20
    sw x0, 0(x2)



    nop
    nop
    nop
    nop
    nop
    nop
