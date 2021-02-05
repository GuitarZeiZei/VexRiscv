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
    val pattern: String = s"0000001----------110-----${custom0}"
    val description: String = "Example Coprocessor"

    def build(controller: EventController): Unit = {

      val regs = controller.prepare event new Area {
        def init = List (0x20, 0x40, 0x60)
      }

      val exec = controller.exec event new Area {
        val counter = Counter(50)
        response.data := (command.cpuRS1.asUInt + command.cpuRS2.asUInt).asBits

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
