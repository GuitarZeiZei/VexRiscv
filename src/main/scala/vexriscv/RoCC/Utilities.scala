/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC

import vexriscv.Stageable
import spinal.core._

/**
  * The custom RISC-V opcodes as described in
  * the user level ISA v2.2 opcode mapping
  */
object CustomOpcodes {
  val custom0 = "0001011"
  val custom1 = "0101011"
  val custom2 = "1011011"
  val custom3 = "1111011"
}

object Utilities {

  def XLEN = 32

    /**
    * Converts a `String` into a Spinal `MaskedLiteral` object. Can
    * be used to on any String instance.
    *
    * Example:
    * {{{
    * "---010".asMaskedLiteral
    * }}}
    * @param s the string to convert
    */
  implicit class MaskedLiteralConversion(val s : String) {
    def asMaskedLiteral : MaskedLiteral = MaskedLiteral.apply(s)
  }

  implicit class CompositeNameReverse(val a : Area) {
    def setCompositeNameRv(prefix : String, nameable: Nameable) : this.type = {
      a.setWeakName(s"${prefix}_event_${nameable.getDisplayName()}").reflectNames()
      this
    }
  }
}
