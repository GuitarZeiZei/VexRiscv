/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC

import spinal.core._

/**
  * The class which defines a co-processor event.
  */
class CoProcessorEvent(name : String) extends Area {

  setWeakName(name)

  /*def outsideCondScope[T](that : => T) : T = {
    val body = Component.current.dslBody
    body.push()
    val swapContext = body.swap()
    val ret = that
    body.pop()
    swapContext.appendBack()
    ret
  }
  */
}