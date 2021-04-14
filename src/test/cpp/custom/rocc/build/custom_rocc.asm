
build/custom_rocc.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	00100e13          	li	t3,1
   4:	3e4cd0b7          	lui	ra,0x3e4cd
   8:	ccc08093          	addi	ra,ra,-820 # 3e4ccccc <pass+0x3e4ccbf4>
   c:	3eccd137          	lui	sp,0x3eccd
  10:	ccc10113          	addi	sp,sp,-820 # 3ecccccc <pass+0x3ecccbf4>
  14:	0020e18b          	0x20e18b
  18:	3f19a237          	lui	tp,0x3f19a
  1c:	99920213          	addi	tp,tp,-1639 # 3f199999 <pass+0x3f1998c1>
  20:	0a321663          	bne	tp,gp,cc <fail>
  24:	00200e13          	li	t3,2
  28:	bfc800b7          	lui	ra,0xbfc80
  2c:	c010a137          	lui	sp,0xc010a
  30:	3d710113          	addi	sp,sp,983 # c010a3d7 <pass+0xc010a2ff>
  34:	0220e18b          	0x220e18b
  38:	00300e13          	li	t3,3
  3c:	be4cd0b7          	lui	ra,0xbe4cd
  40:	ccc08093          	addi	ra,ra,-820 # be4ccccc <pass+0xbe4ccbf4>
  44:	3f4cd137          	lui	sp,0x3f4cd
  48:	ccc10113          	addi	sp,sp,-820 # 3f4ccccc <pass+0x3f4ccbf4>
  4c:	0420e18b          	0x420e18b
  50:	00400e13          	li	t3,4
  54:	010200b7          	lui	ra,0x1020
  58:	30408093          	addi	ra,ra,772 # 1020304 <pass+0x102022c>
  5c:	02040137          	lui	sp,0x2040
  60:	60810113          	addi	sp,sp,1544 # 2040608 <pass+0x2040530>
  64:	0620e18b          	0x620e18b
  68:	00500e13          	li	t3,5
  6c:	123450b7          	lui	ra,0x12345
  70:	67808093          	addi	ra,ra,1656 # 12345678 <pass+0x123455a0>
  74:	87654137          	lui	sp,0x87654
  78:	32110113          	addi	sp,sp,801 # 87654321 <pass+0x87654249>
  7c:	0620e18b          	0x620e18b
  80:	00600e13          	li	t3,6
  84:	bfc800b7          	lui	ra,0xbfc80
  88:	c010a137          	lui	sp,0xc010a
  8c:	3d710113          	addi	sp,sp,983 # c010a3d7 <pass+0xc010a2ff>
  90:	0420e18b          	0x420e18b
  94:	00700e13          	li	t3,7
  98:	be4cd0b7          	lui	ra,0xbe4cd
  9c:	ccc08093          	addi	ra,ra,-820 # be4ccccc <pass+0xbe4ccbf4>
  a0:	3f4cd137          	lui	sp,0x3f4cd
  a4:	ccc10113          	addi	sp,sp,-820 # 3f4ccccc <pass+0x3f4ccbf4>
  a8:	0220e18b          	0x220e18b
  ac:	00800e13          	li	t3,8
  b0:	41bba0b7          	lui	ra,0x41bba
  b4:	99908093          	addi	ra,ra,-1639 # 41bb9999 <pass+0x41bb98c1>
  b8:	41753137          	lui	sp,0x41753
  bc:	f1a10113          	addi	sp,sp,-230 # 41752f1a <pass+0x41752e42>
  c0:	0020e18b          	0x20e18b
  c4:	0220e18b          	0x220e18b
  c8:	0100006f          	j	d8 <pass>

000000cc <fail>:
  cc:	f0100137          	lui	sp,0xf0100
  d0:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffe4c>
  d4:	01c12023          	sw	t3,0(sp)

000000d8 <pass>:
  d8:	f0100137          	lui	sp,0xf0100
  dc:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffe48>
  e0:	00012023          	sw	zero,0(sp)
  e4:	00000013          	nop
  e8:	00000013          	nop
  ec:	00000013          	nop
  f0:	00000013          	nop
  f4:	00000013          	nop
  f8:	00000013          	nop
