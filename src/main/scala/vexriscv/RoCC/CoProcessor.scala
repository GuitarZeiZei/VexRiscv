/**
  * Created on 2021/2/5
  * Define a coprocessor
  */

package vexriscv.RoCC

import spinal.core.{Bundle, Data}

import scala.collection.mutable.ArrayBuffer

/**
  * A computation unit which can be managed by the co processor.
  */
trait CoProcessor {

  var eventController : EventController = null
  val functions = new ArrayBuffer[InstructionFunction[InputBundle, OutputBundle]]

  /**
    * Activate a list of `InstructionFunction` to the computation unit.
    * @param funcs
    * @return
    */
  def activate(funcs : InstructionFunction[InputBundle, OutputBundle]*) = {
    functions ++= funcs
  }

  /**
    * The abstract setup function for the computation unit.
    */
  def setup() : Unit

}
