/**
  * Created on 2021/3/24
  * Instantiation of a Folat Cal coprocessor
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
    val description: String = "Example Coprocessor Float SUB"

    def build(controller: EventController): Unit = {

      val regs = controller.prepare event new Area {
        def init = List (0x20, 0x40, 0x60)
      }

      val exec = controller.exec event new Area {
        val counter = Counter(50)

        val RS1 = command.cpuRS1.noCombLoopCheck
        val RS2 = command.cpuRS2.noCombLoopCheck

        val addorsub = UInt(1 bits)
        when(command.cpuinstruction(26 downto 25) === B"10"){
          addorsub := U(0,1 bits)  //add
        }.otherwise{
          addorsub := U(1,1 bits)  //sub
        }

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
        
        val addsub = new Area{
          val result = new Area{
            val sign = Reg(UInt(1 bits))
            val exp = Reg(UInt(8 bits))
            val mantissa = Reg(UInt(23 bits))
            val manextend = Reg(UInt(25 bits))
            
            val isZero = False
            val isInfinity = False
            val isNaN = False
          }
          val addsubRS1Sign = Reg(UInt(1 bits))
          val addsubRS2Sign = Reg(UInt(1 bits))
          addsubRS1Sign := rs1.sign
          when(addorsub === U(0,1 bits)){
            addsubRS2Sign := rs2.sign
          }.otherwise{
            addsubRS2Sign := rs2.sign + U(1,1 bits)
          }
          // exponent match
          val expRS1GE = rs1.exp >= rs2.exp
          val expDiff = Reg(UInt(8 bits))
          val expMax = Reg(UInt(8 bits))
          when(expRS1GE){
              expDiff := rs1.exp - rs2.exp
              expMax := rs1.exp + U(1, 8 bits)
          }.otherwise{
              expDiff := rs2.exp - rs1.exp
              expMax := rs2.exp + U(1, 8 bits)
          }

          // 对阶
          val MinFraction = Reg(UInt(24 bits))
          val MaxFraction = Reg(UInt(24 bits))
          when(expRS1GE){
            result.sign := addsubRS2Sign
            MinFraction := rs2.manextend |>> expDiff
            MaxFraction := rs1.manextend
          }.otherwise{
            result.sign := addsubRS2Sign
            MinFraction := rs1.manextend |>> expDiff
            MaxFraction := rs2.manextend
          }

          // 尾数相加
          val TempFraction = Reg(UInt(25 bits))
          when(addsubRS2Sign === addsubRS2Sign){
            TempFraction := ((B(0,1 bits) ## MaxFraction).asUInt + (B(0,1 bits) ## MinFraction).asUInt)
          }.otherwise{
            TempFraction := ((B(0,1 bits) ## MaxFraction).asUInt - (B(0,1 bits) ## MinFraction).asUInt)
          }

          // 规格化
          val sel0 = B(0,8 bits) ## TempFraction(24 downto 1)
          val sel1 = Bits(16 bits)
          val sel2 = Bits(8 bits)
          val sel3 = Bits(4 bits)
          val sel4 = Bits(2 bits)
          val pos = Bits(5 bits)
          sel1 := Mux(sel0(31 downto 16).orR, sel0(31 downto 16), sel0(15 downto 0))
          pos(4) := Mux(sel0(31 downto 16).orR, Bool(true), Bool(false))
          sel2 := Mux(sel1(15 downto 8).orR, sel1(15 downto 8), sel1(7 downto 0))
          pos(3) := Mux(sel1(15 downto 8).orR, Bool(true), Bool(false))
          sel3 := Mux(sel2(7 downto 4).orR, sel2(7 downto 4), sel2(3 downto 0))
          pos(2) := Mux(sel2(7 downto 4).orR, Bool(true), Bool(false))
          sel4 := Mux(sel3(3 downto 2).orR, sel2(3 downto 2), sel2(1 downto 0))
          pos(1) := Mux(sel3(3 downto 2).orR, Bool(true), Bool(false))
          pos(0) := Mux(sel4(1), Bool(true), Bool(false))

          val num = Reg(UInt(5 bits))
          val NormF = Reg(UInt(24 bits))
          val NormE = Reg(UInt(8 bits))
          num := U(23,5 bits) - pos.asUInt
          NormF := (sel0(23 downto 0) |<< num).asUInt
          NormE := expMax - num
    
          result.exp := NormE
          result.mantissa := NormF(22 downto 0)
          val data = result.sign.asBits ## result.exp.asBits ## result.mantissa.asBits
        }

        when(command.cpuinstruction(26 downto 25) === B"10"){
          response.data := addsub.data
        }.elsewhen(command.cpuinstruction(26 downto 25) === B"11"){
          response.data := addsub.data
        }.otherwise{
          response.data := (RS1.asUInt + RS2.asUInt).asBits
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
