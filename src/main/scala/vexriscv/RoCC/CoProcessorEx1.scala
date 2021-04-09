/**
  * Created on 2021/2/5
  * Instantiation of a coprocessor
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
    val data = Bits(32 bits)
  }

  def ex1 = new InstructionFunction[Cmd, Rsp](new Cmd(), new Rsp()) {
    val pattern: String = s"0000010----------110-----${custom0}"
    val description: String = "Example Coprocessor FloatAdd"

    def build(controller: EventController): Unit = {

      val regs = controller.prepare event new Area {
        def init = List (0x20, 0x40, 0x60)
      }

      val exec = controller.exec event new Area {
        val counter = Counter(50)

        val RS1 = command.cpuRS1.noCombLoopCheck
        val RS2 = command.cpuRS2.noCombLoopCheck

        val S1 = RS1(31)
        val E1 = RS1(30 downto 23)
        val M1 = B(1, 1 bits) ## RS1(22 downto 0)  //隐藏位
        val S2 = RS2(31)
        val E2 = RS2(30 downto 23)
        val M2 = B(1, 1 bits) ## RS2(22 downto 0)

        val ExpDiff = UInt(8 bits)
        val ExpMax = Bits(8 bits)
        val ExpCompare = Bool

        when(E1.asUInt >= E2.asUInt){
          ExpDiff := E1.asUInt - E2.asUInt
          ExpMax := E1
          ExpCompare := True
        }.otherwise{
          ExpDiff := E2.asUInt - E1.asUInt
          ExpMax := E2
          ExpCompare := False
        }

        val MinFraction = Bits(24 bits)
        val MaxFraction = Bits(24 bits)

        // 符号位判定
        val Sign = Bits(1 bits)
        when(ExpCompare){
          Sign := S1.asBits
        }.otherwise{
          Sign := S2.asBits
        }

        // 对阶
        when(ExpCompare){
          MinFraction := M2 |>> ExpDiff
          MaxFraction := M1
        }.otherwise{
          MinFraction := M1 |>> ExpDiff
          MaxFraction := M2
        }

        // 尾数相加
        val TempFraction = Bits(25 bits)

        when(S1 === S2){
          TempFraction := ((B(0,1 bits) ## MaxFraction).asUInt + (B(0,1 bits) ## MinFraction).asUInt).asBits
        }.otherwise{
          TempFraction := ((B(0,1 bits) ## MaxFraction).asUInt - (B(0,1 bits) ## MinFraction).asUInt).asBits
        }

        // 规格化
        val Exp = Bits(8 bits)
        val Fraction = Bits(23 bits)
        var i = 0
        val ResultFraction = Bits(25 bits)

        // 找到左起第一位非零位，将该位移入隐藏位
        when(!TempFraction(24)){
          i = i + 1
        }

        ResultFraction := TempFraction |<< i

        Fraction := ResultFraction(24 downto 2)
        Exp := (ExpMax.asUInt + i).asBits
        
        response.data := Sign ## Exp ## Fraction
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
