/**
  * Created on 2021/1/21
  * 
  */

package vexriscv.RoCC

import spinal.core._
import spinal.lib._
import vexriscv.plugin._
import vexriscv._
import Utilities._
import org.apache.commons.io.filefilter.FalseFileFilter

import scala.collection.mutable.ArrayBuffer

/**
  * The co processor plugin for VexRiscv. Registers the computation units and
  * creates a `Stageable` for each function.
  *
  * @param c
  */
class RoCCPlugin(c : CoProcessor) extends Plugin[VexRiscv] {

  var cocpu : CoProcessorComp = null
  var functions : ArrayBuffer[InstructionFunction[InputBundle, OutputBundle]] = null
  var stageables : ArrayBuffer[(InstructionFunction[InputBundle, OutputBundle], Stageable[Bool])] = null

  override def setup(pipeline: VexRiscv): Unit = {
    import pipeline.config._

    cocpu = new CoProcessorComp(c)
    cocpu.setDefinitionName("Coprocessor")

    // Collect functions
    functions = cocpu.coprocessor.functions

    stageables = functions.map { f =>
      // Create a singleton stageable and set the name to the provided
      // function name
      object stageable extends Stageable(Bool) {
        //override def setWeakName(name: String): this.type = super.setPartialName(cocpu, f.name)
      }
      f -> stageable
    }

    // Register at decoder service
    val decoder = pipeline.service(classOf[DecoderService])
    for((func, stageable) <- stageables) {
      decoder.addDefault(stageable, False)
      decoder.add(
        key = func.pattern.asMaskedLiteral,
        List(
          stageable -> True,
          REGFILE_WRITE_VALID -> True,
          BYPASSABLE_EXECUTE_STAGE -> False,
          BYPASSABLE_MEMORY_STAGE -> False,
          RS1_USE -> True,
          RS2_USE -> True
        )
      )
    }

  }

  def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    execute plug new Area {
      import execute._

      for((func, stageable) <- stageables) {
        func.io.cmd.valid := False

        // Transfer RS1 and RS2
        func.io.cmd.cpuRS1 := input(RS1)
        func.io.cmd.cpuRS2 := input(RS2)

        // Transfer instruction data
        val hi = func.io.cmd.payload.getBitsWidth
        val lo = (func.io.cmd.payload.getBitsWidth - 32)
        func.io.cmd.payload.assignFromBits(input(INSTRUCTION), hi, lo)

	func.io.cmd.cpuinstruction := input(INSTRUCTION)

        when(arbitration.isValid && input(stageable)) {
          func.io.cmd.valid := !arbitration.isStuckByOthers && !arbitration.removeIt
          arbitration.haltItself := memory.arbitration.isValid && memory.input(stageable)
        }
      }
    }

    memory plug new Area {
      import memory._

      for((func, stageable) <- stageables) {

        func.io.rsp.ready := !arbitration.isStuckByOthers

        when(arbitration.isValid && input(stageable)) {

          // Only stall the pipeline when there is a response to wait for
          if(!func.response.isInstanceOf[InterruptBundle]) {
            arbitration.haltItself := !func.io.rsp.valid
            input(REGFILE_WRITE_DATA) := func.io.rsp.payload.asBits
          }
        }
      }

    }
  }
}
