
build/custom_rocc.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	00100e13          	li	t3,1
   4:	3e4cd0b7          	lui	ra,0x3e4cd
   8:	ccc08093          	addi	ra,ra,-820 # 3e4ccccc <pass+0x3e4ccbf8>
   c:	3eccd137          	lui	sp,0x3eccd
  10:	ccc10113          	addi	sp,sp,-820 # 3ecccccc <pass+0x3ecccbf8>
  14:	0020e18b          	0x20e18b
  18:	3f19a237          	lui	tp,0x3f19a
  1c:	99920213          	addi	tp,tp,-1639 # 3f199999 <pass+0x3f1998c5>
  20:	0a321463          	bne	tp,gp,c8 <fail>
  24:	00200e13          	li	t3,2
  28:	bfc800b7          	lui	ra,0xbfc80
  2c:	c010a137          	lui	sp,0xc010a
  30:	3d710113          	addi	sp,sp,983 # c010a3d7 <pass+0xc010a303>
  34:	0220e18b          	0x220e18b
  38:	00300e13          	li	t3,3
  3c:	be4cd0b7          	lui	ra,0xbe4cd
  40:	ccc08093          	addi	ra,ra,-820 # be4ccccc <pass+0xbe4ccbf8>
  44:	3f4cd137          	lui	sp,0x3f4cd
  48:	ccc10113          	addi	sp,sp,-820 # 3f4ccccc <pass+0x3f4ccbf8>
  4c:	0420e18b          	0x420e18b
  50:	00400e13          	li	t3,4
  54:	41bba0b7          	lui	ra,0x41bba
  58:	99908093          	addi	ra,ra,-1639 # 41bb9999 <pass+0x41bb98c5>
  5c:	41753137          	lui	sp,0x41753
  60:	f1a10113          	addi	sp,sp,-230 # 41752f1a <pass+0x41752e46>
  64:	0620e18b          	0x620e18b
  68:	00500e13          	li	t3,5
  6c:	bfc800b7          	lui	ra,0xbfc80
  70:	c010a137          	lui	sp,0xc010a
  74:	3d710113          	addi	sp,sp,983 # c010a3d7 <pass+0xc010a303>
  78:	0620e18b          	0x620e18b
  7c:	00600e13          	li	t3,6
  80:	bfc800b7          	lui	ra,0xbfc80
  84:	c010a137          	lui	sp,0xc010a
  88:	3d710113          	addi	sp,sp,983 # c010a3d7 <pass+0xc010a303>
  8c:	0420e18b          	0x420e18b
  90:	00700e13          	li	t3,7
  94:	be4cd0b7          	lui	ra,0xbe4cd
  98:	ccc08093          	addi	ra,ra,-820 # be4ccccc <pass+0xbe4ccbf8>
  9c:	3f4cd137          	lui	sp,0x3f4cd
  a0:	ccc10113          	addi	sp,sp,-820 # 3f4ccccc <pass+0x3f4ccbf8>
  a4:	0220e18b          	0x220e18b
  a8:	00800e13          	li	t3,8
  ac:	41bba0b7          	lui	ra,0x41bba
  b0:	99908093          	addi	ra,ra,-1639 # 41bb9999 <pass+0x41bb98c5>
  b4:	41753137          	lui	sp,0x41753
  b8:	f1a10113          	addi	sp,sp,-230 # 41752f1a <pass+0x41752e46>
  bc:	0020e18b          	0x20e18b
  c0:	0220e18b          	0x220e18b
  c4:	0100006f          	j	d4 <pass>

000000c8 <fail>:
  c8:	f0100137          	lui	sp,0xf0100
  cc:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffe50>
  d0:	01c12023          	sw	t3,0(sp)

000000d4 <pass>:
  d4:	f0100137          	lui	sp,0xf0100
  d8:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffe4c>
  dc:	00012023          	sw	zero,0(sp)
  e0:	00000013          	nop
  e4:	00000013          	nop
  e8:	00000013          	nop
  ec:	00000013          	nop
  f0:	00000013          	nop
  f4:	00000013          	nop
