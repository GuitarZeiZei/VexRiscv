
build/custom_rocc.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	00100e13          	li	t3,1
   4:	0200608b          	0x200608b
   8:	06009c63          	bnez	ra,80 <fail>
   c:	00200e13          	li	t3,2
  10:	00000093          	li	ra,0
  14:	00000113          	li	sp,0
  18:	0220e18b          	0x220e18b
  1c:	06119263          	bne	gp,ra,80 <fail>
  20:	00300e13          	li	t3,3
  24:	010200b7          	lui	ra,0x1020
  28:	30408093          	addi	ra,ra,772 # 1020304 <pass+0x1020278>
  2c:	00000113          	li	sp,0
  30:	0220e18b          	0x220e18b
  34:	04119663          	bne	gp,ra,80 <fail>
  38:	00400e13          	li	t3,4
  3c:	03061237          	lui	tp,0x3061
  40:	90c20213          	addi	tp,tp,-1780 # 306090c <pass+0x3060880>
  44:	010200b7          	lui	ra,0x1020
  48:	30408093          	addi	ra,ra,772 # 1020304 <pass+0x1020278>
  4c:	02040137          	lui	sp,0x2040
  50:	60810113          	addi	sp,sp,1544 # 2040608 <pass+0x204057c>
  54:	0220e18b          	0x220e18b
  58:	02419463          	bne	gp,tp,80 <fail>
  5c:	00500e13          	li	t3,5
  60:	00600293          	li	t0,6
  64:	00100093          	li	ra,1
  68:	00200113          	li	sp,2
  6c:	00300193          	li	gp,3
  70:	0220e08b          	0x220e08b
  74:	0230e08b          	0x230e08b
  78:	00509463          	bne	ra,t0,80 <fail>
  7c:	0100006f          	j	8c <pass>

00000080 <fail>:
  80:	f0100137          	lui	sp,0xf0100
  84:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffe98>
  88:	01c12023          	sw	t3,0(sp)

0000008c <pass>:
  8c:	f0100137          	lui	sp,0xf0100
  90:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffe94>
  94:	00012023          	sw	zero,0(sp)
  98:	00000013          	nop
  9c:	00000013          	nop
  a0:	00000013          	nop
  a4:	00000013          	nop
  a8:	00000013          	nop
  ac:	00000013          	nop
