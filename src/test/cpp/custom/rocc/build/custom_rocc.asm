
build/custom_rocc.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	00400e13          	li	t3,4
   4:	010200b7          	lui	ra,0x1020
   8:	30408093          	addi	ra,ra,772 # 1020304 <pass+0x1020298>
   c:	02040137          	lui	sp,0x2040
  10:	60810113          	addi	sp,sp,1544 # 2040608 <pass+0x204059c>
  14:	0220e18b          	0x220e18b
  18:	00100e13          	li	t3,1
  1c:	3e4cd0b7          	lui	ra,0x3e4cd
  20:	ccc08093          	addi	ra,ra,-820 # 3e4ccccc <pass+0x3e4ccc60>
  24:	3eccd137          	lui	sp,0x3eccd
  28:	ccc10113          	addi	sp,sp,-820 # 3ecccccc <pass+0x3ecccc60>
  2c:	0420e18b          	0x420e18b
  30:	00200e13          	li	t3,2
  34:	bfc800b7          	lui	ra,0xbfc80
  38:	c010a137          	lui	sp,0xc010a
  3c:	3d710113          	addi	sp,sp,983 # c010a3d7 <pass+0xc010a36b>
  40:	0420e18b          	0x420e18b
  44:	00300e13          	li	t3,3
  48:	be4cd0b7          	lui	ra,0xbe4cd
  4c:	ccc08093          	addi	ra,ra,-820 # be4ccccc <pass+0xbe4ccc60>
  50:	3f4cd137          	lui	sp,0x3f4cd
  54:	ccc10113          	addi	sp,sp,-820 # 3f4ccccc <pass+0x3f4ccc60>
  58:	0420e18b          	0x420e18b
  5c:	0100006f          	j	6c <pass>

00000060 <fail>:
  60:	f0100137          	lui	sp,0xf0100
  64:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffeb8>
  68:	01c12023          	sw	t3,0(sp)

0000006c <pass>:
  6c:	f0100137          	lui	sp,0xf0100
  70:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffeb4>
  74:	00012023          	sw	zero,0(sp)
  78:	00000013          	nop
  7c:	00000013          	nop
  80:	00000013          	nop
  84:	00000013          	nop
  88:	00000013          	nop
  8c:	00000013          	nop
