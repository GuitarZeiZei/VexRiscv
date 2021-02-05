/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC
import spinal.core._

import scala.collection.mutable.ArrayBuffer


trait  EventController {
  type T <: EventController

  var coprocessor : CoProcessor = null
  var eventController : EventController = this

  val events = new ArrayBuffer[CoProcessorEvent]
  events += new CoProcessorEvent("prepare")
  events += new CoProcessorEvent("exec")

  val prepare = events(0)
  val exec = events(1)

  def build() : Unit = {
    coprocessor.eventController = this
    coprocessor.setup()

    // All functions are registered at this point
    coprocessor.functions.foreach(f => f.setup())
    coprocessor.functions.foreach(f => f.build(eventController))
  }

  Component.current.addPrePopTask(() => build())
}