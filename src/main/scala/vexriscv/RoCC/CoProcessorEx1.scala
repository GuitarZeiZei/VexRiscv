/**
  * Created on 2021/3/24
  * Instantiation of a MAC coprocessor
  */

package vexriscv.RoCC
import spinal.core._
import spinal.lib.Counter
import vexriscv.RoCC.CustomOpcodes._


class CoProcessorEx1 extends CoProcessor {

  case class Cmd() extends InputBundle {
    val funct = Bits(7 bits)
    val rs2 = Bits(5 bits)
    val rs1 = Bits(5 bits)
    val xd = Bool()
    val xs1 = Bool()
    val xs2 = Bool()
    val rd = Bits(5 bits)
    val opcode = Bits(7 bits)
  }

  case class Rsp() extends OutputBundle {
    val data = Bits(32 bits).noCombLoopCheck
  }


  def ex1 = new InstructionFunction[Cmd, Rsp](new Cmd(), new Rsp()) {
    // val pattern: String = s"0000001----------110-----${custom0}"
    val pattern: String = s"00000------------110-----${custom0}"
    val description: String = "Example Coprocessor Float ADD"

    def build(controller: EventController): Unit = {

      val regs = controller.prepare event new Area {
        def init = List (0x20, 0x40, 0x60)
      }

      val exec = controller.exec event new Area {
        val counter = Counter(50)

        val RS1 = command.cpuRS1.noCombLoopCheck
        val RS2 = command.cpuRS2.noCombLoopCheck

        val rs1 = new Area{
          // 原始符号位、指数、小数部分
          val sign = RS1(31).asUInt
          val exp = RS1(30 downto 23).asUInt
          val mantissa = RS1(22 downto 0).asUInt
          // 带隐藏位的小数部分
          val manextend = (B(1, 1 bits) ## mantissa.asBits).asUInt
          
          val manZero = mantissa === 0
          val expZero = exp === 0   // 指数全为0
          val expOne = exp.andR     // 指数全为1
          
          val isZero = expZero && manZero
          val isSubnormal = expZero && !manZero
          val isInfinity = expOne && manZero
          val isNaN = expOne && !manZero
          
          // 接近0的小数，隐藏位为0
          when(isSubnormal){
              manextend := (B(0, 1 bits) ## mantissa.asBits).asUInt
          }
        }
    
        val rs2 = new Area{
          // 原始符号位、指数、小数部分
          val sign = RS2(31).asUInt
          val exp = RS2(30 downto 23).asUInt
          val mantissa = RS2(22 downto 0).asUInt
          // 带隐藏位的小数部分
          val manextend = (B(1, 1 bits) ## mantissa.asBits).asUInt
          
          val manZero = mantissa === 0
          val expZero = exp === 0   // 指数全为0
          val expOne = exp.andR     // 指数全为1
          
          val isZero = expZero && manZero
          val isSubnormal = expZero && !manZero
          val isInfinity = expOne && manZero
          val isNaN = expOne && !manZero
          
          // 接近0的小数，隐藏位为0
          when(isSubnormal){
              manextend := (B(0, 1 bits) ## mantissa.asBits).asUInt
          }
        }
        
        val result = new Area{
            val sign = UInt(1 bits)
            val exp = UInt(8 bits)
            val mantissa = UInt(23 bits)
            val manextend = UInt(25 bits)
            
            val isZero = False
            val isInfinity = False
            val isNaN = False
        }
        
        val add = new Area{
            // exponent match
            val expRS1GE = rs1.exp >= rs2.exp
            val expDiff = UInt(8 bits)
            val expMax = UInt(8 bits)
            when(expRS1GE){
                expDiff := rs1.exp - rs2.exp
                expMax := rs1.exp + U(1, 8 bits)
            }.otherwise{
                expDiff := rs2.exp - rs1.exp
                expMax := rs2.exp + U(1, 8 bits)
            }

            // 对阶
            val MinFraction = UInt(24 bits)
            val MaxFraction = UInt(24 bits)
            when(expRS1GE){
              result.sign := rs1.sign
              MinFraction := rs2.manextend |>> expDiff
              MaxFraction := rs1.manextend
            }.otherwise{
              result.sign := rs2.sign
              MinFraction := rs1.manextend |>> expDiff
              MaxFraction := rs2.manextend
            }

            // 尾数相加
            val TempFraction = UInt(25 bits)
            when(rs1.sign === rs2.sign){
              TempFraction := ((B(0,1 bits) ## MaxFraction).asUInt + (B(0,1 bits) ## MinFraction).asUInt)
            }.otherwise{
              TempFraction := ((B(0,1 bits) ## MaxFraction).asUInt - (B(0,1 bits) ## MinFraction).asUInt)
            }

			// 规格化
			val TempF_0 = TempFraction(24 downto 1)
			val TempE_0 = expMax
			val TempF_1 = UInt(24 bits)
			val TempE_1 = UInt(8 bits)
			when(!TempF_0(23)){
				TempF_1 := TempF_0 |<< 1
				TempE_1 := TempE_0 - U(1, 8 bits)
			}.otherwise{
				TempF_1 := TempF_0
				TempE_1 := TempE_0
			}
			
			val TempF_2 = UInt(24 bits)
			val TempE_2 = UInt(8 bits)
			when(!TempF_1(23)){
				TempF_2 := TempF_1 |<< 1
				TempE_2 := TempE_1 - U(1, 8 bits)
			}.otherwise{
				TempF_2 := TempF_1
				TempE_2 := TempE_1
			}

			val TempF_3 = UInt(24 bits)
			val TempE_3 = UInt(8 bits)
			when(!TempF_2(23)){
				TempF_3 := TempF_2 |<< 1
				TempE_3 := TempE_2 - U(1, 8 bits)
			}.otherwise{
				TempF_3 := TempF_2
				TempE_3 := TempE_2
			}

			val TempF_4 = UInt(24 bits)
			val TempE_4 = UInt(8 bits)
			when(!TempF_3(23)){
				TempF_4 := TempF_3 |<< 1
				TempE_4 := TempE_3 - U(1, 8 bits)
			}.otherwise{
				TempF_4 := TempF_3
				TempE_4 := TempE_3
			}

			val TempF_5 = UInt(24 bits)
			val TempE_5 = UInt(8 bits)
			when(!TempF_4(23)){
				TempF_5 := TempF_4 |<< 1
				TempE_5 := TempE_4 - U(1, 8 bits)
			}.otherwise{
				TempF_5 := TempF_4
				TempE_5 := TempE_4
			}

			val TempF_6 = UInt(24 bits)
			val TempE_6 = UInt(8 bits)
			when(!TempF_5(23)){
				TempF_6 := TempF_5 |<< 1
				TempE_6 := TempE_5 - U(1, 8 bits)
			}.otherwise{
				TempF_6 := TempF_5
				TempE_6 := TempE_5
			}

			val TempF_7 = UInt(24 bits)
			val TempE_7 = UInt(8 bits)
			when(!TempF_6(23)){
				TempF_7 := TempF_6 |<< 1
				TempE_7 := TempE_6 - U(1, 8 bits)
			}.otherwise{
				TempF_7 := TempF_6
				TempE_7 := TempE_6
			}

			val TempF_8 = UInt(24 bits)
			val TempE_8 = UInt(8 bits)
			when(!TempF_7(23)){
				TempF_8 := TempF_7 |<< 1
				TempE_8 := TempE_7 - U(1, 8 bits)
			}.otherwise{
				TempF_8 := TempF_7
				TempE_8 := TempE_7
			}

			val TempF_9 = UInt(24 bits)
			val TempE_9 = UInt(8 bits)
			when(!TempF_8(23)){
				TempF_9 := TempF_8 |<< 1
				TempE_9 := TempE_8 - U(1, 8 bits)
			}.otherwise{
				TempF_9 := TempF_8
				TempE_9 := TempE_8
			}

			val TempF_10 = UInt(24 bits)
			val TempE_10 = UInt(8 bits)
			when(!TempF_9(23)){
				TempF_10 := TempF_9 |<< 1
				TempE_10 := TempE_9 - U(1, 8 bits)
			}.otherwise{
				TempF_10 := TempF_9
				TempE_10 := TempE_9
			}

			val TempF_11 = UInt(24 bits)
			val TempE_11 = UInt(8 bits)
			when(!TempF_10(23)){
				TempF_11 := TempF_10 |<< 1
				TempE_11 := TempE_10 - U(1, 8 bits)
			}.otherwise{
				TempF_11 := TempF_10
				TempE_11 := TempE_10
			}

			val TempF_12 = UInt(24 bits)
			val TempE_12 = UInt(8 bits)
			when(!TempF_11(23)){
				TempF_12 := TempF_11 |<< 1
				TempE_12 := TempE_11 - U(1, 8 bits)
			}.otherwise{
				TempF_12 := TempF_11
				TempE_12 := TempE_11
			}

			val TempF_13 = UInt(24 bits)
			val TempE_13 = UInt(8 bits)
			when(!TempF_12(23)){
				TempF_13 := TempF_12 |<< 1
				TempE_13 := TempE_12 - U(1, 8 bits)
			}.otherwise{
				TempF_13 := TempF_12
				TempE_13 := TempE_12
			}

			val TempF_14 = UInt(24 bits)
			val TempE_14 = UInt(8 bits)
			when(!TempF_13(23)){
				TempF_14 := TempF_13 |<< 1
				TempE_14 := TempE_13 - U(1, 8 bits)
			}.otherwise{
				TempF_14 := TempF_13
				TempE_14 := TempE_13
			}

			val TempF_15 = UInt(24 bits)
			val TempE_15 = UInt(8 bits)
			when(!TempF_14(23)){
				TempF_15 := TempF_14 |<< 1
				TempE_15 := TempE_14 - U(1, 8 bits)
			}.otherwise{
				TempF_15 := TempF_14
				TempE_15 := TempE_14
			}

			val TempF_16 = UInt(24 bits)
			val TempE_16 = UInt(8 bits)
			when(!TempF_15(23)){
				TempF_16 := TempF_15 |<< 1
				TempE_16 := TempE_15 - U(1, 8 bits)
			}.otherwise{
				TempF_16 := TempF_15
				TempE_16 := TempE_15
			}

			val TempF_17 = UInt(24 bits)
			val TempE_17 = UInt(8 bits)
			when(!TempF_16(23)){
				TempF_17 := TempF_16 |<< 1
				TempE_17 := TempE_16 - U(1, 8 bits)
			}.otherwise{
				TempF_17 := TempF_16
				TempE_17 := TempE_16
			}

			val TempF_18 = UInt(24 bits)
			val TempE_18 = UInt(8 bits)
			when(!TempF_17(23)){
				TempF_18 := TempF_17 |<< 1
				TempE_18 := TempE_17 - U(1, 8 bits)
			}.otherwise{
				TempF_18 := TempF_17
				TempE_18 := TempE_17
			}

			val TempF_19 = UInt(24 bits)
			val TempE_19 = UInt(8 bits)
			when(!TempF_18(23)){
				TempF_19 := TempF_18 |<< 1
				TempE_19 := TempE_18 - U(1, 8 bits)
			}.otherwise{
				TempF_19 := TempF_18
				TempE_19 := TempE_18
			}

			val TempF_20 = UInt(24 bits)
			val TempE_20 = UInt(8 bits)
			when(!TempF_19(23)){
				TempF_20 := TempF_19 |<< 1
				TempE_20 := TempE_19 - U(1, 8 bits)
			}.otherwise{
				TempF_20 := TempF_19
				TempE_20 := TempE_19
			}

			val TempF_21 = UInt(24 bits)
			val TempE_21 = UInt(8 bits)
			when(!TempF_20(23)){
				TempF_21 := TempF_20 |<< 1
				TempE_21 := TempE_20 - U(1, 8 bits)
			}.otherwise{
				TempF_21 := TempF_20
				TempE_21 := TempE_20
			}

			val TempF_22 = UInt(24 bits)
			val TempE_22 = UInt(8 bits)
			when(!TempF_21(23)){
				TempF_22 := TempF_21 |<< 1
				TempE_22 := TempE_21 - U(1, 8 bits)
			}.otherwise{
				TempF_22 := TempF_21
				TempE_22 := TempE_21
			}

			val TempF_23 = UInt(24 bits)
			val TempE_23 = UInt(8 bits)
			when(!TempF_22(23)){
				TempF_23 := TempF_22 |<< 1
				TempE_23 := TempE_22 - U(1, 8 bits)
			}.otherwise{
				TempF_23 := TempF_22
				TempE_23 := TempE_22
			}

			// val TempF_24 = UInt(24 bits)
			// val TempE_24 = UInt(8 bits)
			// when(!TempF_23(23)){
			// 	TempF_24 := TempF_23 |<< 1
			// 	TempE_24 := TempE_23 - U(1, 8 bits)
			// }.otherwise{
			// 	TempF_24 := TempF_23
			// 	TempE_24 := TempE_23
			// }
			
			result.exp := TempE_23
			result.mantissa := TempF_23(22 downto 0)
            val data = result.sign.asBits ## result.exp.asBits ## result.mantissa.asBits
        }
        
        switch(command.cpuinstruction(26 downto 25)) {
          is(B"10") {
            response.data := add.data
          }
          default {
            response.data := (RS1.asUInt + RS2.asUInt).asBits
          }
        }
        
        when(counter.willOverflowIfInc) {
          flush := True
        }

        when(!done) {
          counter.increment()
        }.otherwise {
          counter.clear()
        }
      }
    }
  }

  def setup(): Unit = {
	  activate(ex1)
  }
}
