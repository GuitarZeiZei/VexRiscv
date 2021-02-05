/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC

import spinal.core._

/***
  * With the `EventController` trait the Co processor defines
  * a fixed set of events the computation units can plug into.
  */
class CoProcessorComp(c : CoProcessor) extends Component with EventController {
  type T = CoProcessorComp

  setWeakName("cocpu")
  coprocessor = c
}