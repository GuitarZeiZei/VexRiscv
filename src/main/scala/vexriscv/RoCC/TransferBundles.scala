/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC

import spinal.core._
import Utilities._


class InputBundle extends Bundle {
  val cpuRS1 = Bits(XLEN bits)
  val cpuRS2 = Bits(XLEN bits)
  val cpuinstruction = Bits(XLEN bits)
}

class OutputBundle extends Bundle
class InterruptBundle extends OutputBundle
