.globl _start
_start:

#define r_type_insn(_f7, _rs2, _rs1, _f3, _rd, _opc) \
.word (((_f7) << 25) | ((_rs2) << 20) | ((_rs1) << 15) | ((_f3) << 12) | ((_rd) << 7) | ((_opc) << 0))

#define RoCCFadd(_rd, _rs1, _rs2 ) \
r_type_insn(0b0000000, _rs2, _rs1, 0b110, _rd, 0b0001011)

#define RoCCFsub(_rd, _rs1, _rs2 ) \
r_type_insn(0b0000001, _rs2, _rs1, 0b110, _rd, 0b0001011)

#define RoCCFmul(_rd, _rs1, _rs2 ) \
r_type_insn(0b0000010, _rs2, _rs1, 0b110, _rd, 0b0001011)

#define RoCC(_rd, _rs1, _rs2 ) \
r_type_insn(0b0000011, _rs2, _rs1, 0b110, _rd, 0b0001011)


//TEST 1
	li x28, 1
	li x1,0x3E4CCCCC  //0.2
	li x2,0x3ECCCCCC  //0.4
	RoCCFadd(3,1,2)
	li x4,0x3F199999
	bne x4,x3,fail

//TEST 2
	li x28, 2
	li x1,0xBFC80000  //-1.5625
	li x2,0xC010A3D7  //-2.26
	RoCCFsub(3,1,2)   //0.6975

//TEST 3
	li x28, 3
	li x1,0xBE4CCCCC  //-0.2
	li x2,0x3F4CCCCC  //0.8
	RoCCFmul(3,1,2)

//TEST 4
	li x28, 4
	li x1,0x01020304
	li x2,0x02040608
	RoCC(3,1,2)

//TEST 5
	li x28, 5
	li x1,0x12345678
	li x2,0x87654321
	RoCC(3,1,2)

//TEST 6
	li x28, 6
	li x1,0xBFC80000  //-1.5625
	li x2,0xC010A3D7  //-2.26
	RoCCFmul(3,1,2)

//TEST 7
	li x28, 7
	li x1,0xBE4CCCCC  //-0.2
	li x2,0x3F4CCCCC  //0.8
	RoCCFsub(3,1,2)

//TEST 8
	li x28, 8
	li x1,0x41BB9999  //23.45
	li x2,0x41752F1A  //15.324
	RoCCFadd(3,1,2)
	RoCCFsub(3,1,2)   //8.126

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
