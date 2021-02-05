/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC

import spinal.core._
import spinal.lib._
import Utilities._

trait FunctionDef extends Nameable {
  val pattern : String
  val description : String
  override def getName() = name
}

abstract class InstructionFunction[+A <: InputBundle, +B <: OutputBundle](dtCmd : A, dtRsp : B) extends FunctionDef {
  val io = new Bundle {
    val cmd = slave Stream (dtCmd.asInstanceOf[InputBundle])
    val rsp = master Stream (dtRsp.asInstanceOf[OutputBundle])
  }

  // Payload of the command, prepared as specific bundle
  private[this] val cmdPayloadReg = Reg(Bits(io.cmd.payload.getBitsWidth bits))
  val command = cloneOf(dtCmd)
  command.assignFromBits(cmdPayloadReg.asBits)

  // Payload of the response, prepared as specific bundle
  val response = cloneOf(dtRsp)
  io.rsp.payload.assignFromBits(response.asBits)

  val flush = RegInit(False)
  val done = RegInit(True)

  // Communication of the Ready/Valid-Interface
  io.cmd.ready := False
  io.rsp.valid := flush // Send response when user sets flush

  when(io.rsp.ready){
    flush := False
  }

  when(done) {
    when(!flush || io.rsp.ready) { // New command
      done := !io.cmd.valid
      flush := False
      io.cmd.ready := True
      io.rsp.payload.assignFromBits(B(io.rsp.payload.getBitsWidth bits, default -> false))
      cmdPayloadReg := io.cmd.payload.asBits
    }
  }.otherwise{
    when(flush){
      done := True
      io.cmd.ready := True
    }
  }

  def setup(): Unit = {
    io.cmd.setWeakName(s"${name}_communication_cmd")
    io.rsp.setWeakName(s"${name}_communication_rsp")
    flush.setWeakName(s"${name}_communication_flush")
    done.setWeakName(s"${name}_communication_done")
    cmdPayloadReg.setWeakName(s"${name}_communication_cmdPayloadReg")
  }


  def build(controller : EventController) : Unit

    /**
    * Implicit class to create the event areas
    * @param ev the event to define
    */
  implicit class implicitsEvent(ev: CoProcessorEvent){
    def event[T <: Area](area : T) : T = {area.setCompositeNameRv(getName(), ev);area}
  }
}