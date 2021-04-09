
build/custom_rocc.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	00100e13          	li	t3,1
   4:	3e4cd0b7          	lui	ra,0x3e4cd
   8:	ccc08093          	addi	ra,ra,-820 # 3e4ccccc <pass+0x3e4ccc98>
   c:	3eccd137          	lui	sp,0x3eccd
  10:	ccc10113          	addi	sp,sp,-820 # 3ecccccc <pass+0x3ecccc98>
  14:	3f19a237          	lui	tp,0x3f19a
  18:	99920213          	addi	tp,tp,-1639 # 3f199999 <pass+0x3f199965>
  1c:	0420e18b          	0x420e18b
  20:	00419463          	bne	gp,tp,28 <fail>
  24:	0100006f          	j	34 <pass>

00000028 <fail>:
  28:	f0100137          	lui	sp,0xf0100
  2c:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffef0>
  30:	01c12023          	sw	t3,0(sp)

00000034 <pass>:
  34:	f0100137          	lui	sp,0xf0100
  38:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffeec>
  3c:	00012023          	sw	zero,0(sp)
  40:	00000013          	nop
  44:	00000013          	nop
  48:	00000013          	nop
  4c:	00000013          	nop
  50:	00000013          	nop
  54:	00000013          	nop
