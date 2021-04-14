/**
  * Created on 2021/3/24
  * Instantiation of a Folat Cal coprocessor
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
    val data = Bits(32 bits).noCombLoopCheck
  }


  def ex1 = new InstructionFunction[Cmd, Rsp](new Cmd(), new Rsp()) {
    // val pattern: String = s"0000001----------110-----${custom0}"
    val pattern: String = s"00000------------110-----${custom0}"
    val description: String = "Example Coprocessor Float Calculation"

    def build(controller: EventController): Unit = {

      val regs = controller.prepare event new Area {
        def init = List (0x20, 0x40, 0x60)
      }

      val exec = controller.exec event new Area {
        val counter = Counter(50)

        val RS1 = command.cpuRS1.noCombLoopCheck
        val RS2 = command.cpuRS2.noCombLoopCheck

        val addorsub = Bool
        when(command.cpuinstruction(26 downto 25) === B"00"){
          addorsub := Bool(false)  //add
        }.otherwise{
          addorsub := Bool(true)  //sub
        }

        val rs1 = new Area{
          // 原始符号位、指数、小数部分
          val sign = RS1(31).asUInt
          val exp = RS1(30 downto 23).asUInt
          val mantissa = RS1(22 downto 0).asUInt
          // 带隐藏位的小数部分
          val manextend = (B(1, 1 bits) ## mantissa.asBits).asUInt
          
          val manZero = mantissa === 0
          val expZero = exp === 0   // 指数全为0
          val expOne = exp.andR     // 指数全为1
          
          val isZero = expZero && manZero
          val isSubnormal = expZero && !manZero
          val isInfinity = expOne && manZero
          val isNaN = expOne && !manZero
          
          // 接近0的小数，隐藏位为0
          when(isSubnormal){
              manextend := (B(0, 1 bits) ## mantissa.asBits).asUInt
          }
        }
    
        val rs2 = new Area{
          // 原始符号位、指数、小数部分
          val sign = RS2(31).asUInt
          val exp = RS2(30 downto 23).asUInt
          val mantissa = RS2(22 downto 0).asUInt
          // 带隐藏位的小数部分
          val manextend = (B(1, 1 bits) ## mantissa.asBits).asUInt
          
          val manZero = mantissa === 0
          val expZero = exp === 0   // 指数全为0
          val expOne = exp.andR     // 指数全为1
          
          val isZero = expZero && manZero
          val isSubnormal = expZero && !manZero
          val isInfinity = expOne && manZero
          val isNaN = expOne && !manZero
          
          // 接近0的小数，隐藏位为0
          when(isSubnormal){
              manextend := (B(0, 1 bits) ## mantissa.asBits).asUInt
          }
        }
        
        val addsub = new Area{
          val Comp_enable = Bool
          val operand_a = Bits(32 bits)
          val operand_b = Bits(32 bits)
          //for operations always operand_a must not be less than b_operand
          when(RS1(30 downto 0).asUInt < RS2(30 downto 0).asUInt){
            Comp_enable := Bool(true)
            operand_a := RS2
            operand_b := RS1
          }.otherwise{
            Comp_enable := Bool(false)
            operand_a := RS1
            operand_b := RS2
          }
          // val exp_a = operand_a(30 downto 23)
          // val exp_b = operand_b(30 downto 23)

          //Exception flag sets 1 if either one of the exponent is 255.
          val Exception = (operand_a(30 downto 23).andR) | (operand_b(30 downto 23).andR)
          val output_sign = UInt(1 bits)
          when(addorsub){
            when(Comp_enable){
              output_sign := (!operand_a(31)).asUInt
            }.otherwise{
              output_sign := operand_a(31).asUInt
            }
          }.otherwise{
            output_sign := operand_a(31).asUInt
          }
          val operation_sub_addBar = Bool
          when(addorsub){
            operation_sub_addBar := operand_a(31) ^ operand_b(31)
          }.otherwise{
            operation_sub_addBar := !(operand_a(31) ^ operand_b(31))
          }
          //Assigining significand values according to Hidden Bit.
          //If exponent is equal to zero then hidden bit will be 0 for that respective significand else it will be 1
          val significand_a = Mux(operand_a(30 downto 23).orR, B(1,1 bits)##operand_a(22 downto 0), B(0,1 bits)##operand_a(22 downto 0))
          val significand_b = Mux(operand_b(30 downto 23).orR, B(1,1 bits)##operand_b(22 downto 0), B(0,1 bits)##operand_b(22 downto 0))
          //Evaluating Exponent Difference
          val exponent_diff = operand_a(30 downto 23).asUInt - operand_b(30 downto 23).asUInt
          //Shifting significand_b according to exponent_diff
          val significand_b_add_sub = significand_b |>> exponent_diff
          val exponent_b_add_sub = operand_b(30 downto 23).asUInt + exponent_diff

          //Checking exponents are same or not
          val perform = operand_a(30 downto 23) === exponent_b_add_sub.asBits

          ///////////////////////////////////////////////////////////////////////////////////////////////////////
          //------------------------------------------------ADD BLOCK------------------------------------------//
          val significand_add = Mux(perform & operation_sub_addBar, significand_a.asUInt.resize(25) + significand_b_add_sub.asUInt.resize(25), U(0,25 bits))
          
          //Result will be equal to Most 23 bits if carry generates else it will be Least 22 bits.
          val add_sum = UInt(31 bits)
          add_sum(22 downto 0) := Mux(significand_add(24), significand_add(23 downto 1), significand_add(22 downto 0))
          
          //If carry generates in sum value then exponent must be added with 1 else feed as it is.
          add_sum(30 downto 23) := Mux(significand_add(24),U(1,8 bits)+operand_a(30 downto 23).asUInt,operand_a(30 downto 23).asUInt)
          

          ///////////////////////////////////////////////////////////////////////////////////////////////////////
          //------------------------------------------------SUB BLOCK------------------------------------------//
          val significand_sub_complement = Mux(perform & !operation_sub_addBar, (~significand_b_add_sub).asUInt+U(1,24 bits), U(0,24 bits))
          val significand_sub = Mux(perform, significand_a.asUInt.resize(25) + significand_sub_complement.resize(25), U(0,25 bits))

          // subnormlize
          val shift = Reg(UInt(5 bits))
          val subtraction_diff = Reg(UInt(25 bits))
          val exponent_sub = Reg(UInt(8 bits))
          when(!significand_sub(24)){
            subtraction_diff := ~significand_sub + U(1,25 bits)
            shift := U(0,5 bits)
          }.elsewhen(!(significand_sub(23 downto 0).orR)){
            subtraction_diff := significand_sub |<< 24
            shift := U(24,5 bits)
          }.otherwise{
            val sel0 = B(0,8 bits) ## significand_sub(23 downto 0)
            // val sel0 = B(0,8 bits) ## significand_sub(24 downto 1)
            val sel1 = Bits(16 bits)
            val sel2 = Bits(8 bits)
            val sel3 = Bits(4 bits)
            val sel4 = Bits(2 bits)
            val pos = Bits(5 bits)
            sel1 := Mux(sel0(31 downto 16).orR, sel0(31 downto 16), sel0(15 downto 0))
            pos(4) := Mux(sel0(31 downto 16).orR, Bool(true), Bool(false))
            sel2 := Mux(sel1(15 downto 8).orR, sel1(15 downto 8), sel1(7 downto 0))
            pos(3) := Mux(sel1(15 downto 8).orR, Bool(true), Bool(false))
            sel3 := Mux(sel2(7 downto 4).orR, sel2(7 downto 4), sel2(3 downto 0))
            pos(2) := Mux(sel2(7 downto 4).orR, Bool(true), Bool(false))
            sel4 := Mux(sel3(3 downto 2).orR, sel3(3 downto 2), sel3(1 downto 0))
            pos(1) := Mux(sel3(3 downto 2).orR, Bool(true), Bool(false))
            pos(0) := Mux(sel4(1), Bool(true), Bool(false))
            shift := U(23,5 bits) - pos.asUInt
            subtraction_diff := significand_sub |<< shift
          }
          exponent_sub := operand_a(30 downto 23).asUInt - shift
          val sub_diff = UInt(31 bits)
          sub_diff(30 downto 23) := exponent_sub
          sub_diff(22 downto 0) := subtraction_diff(22 downto 0)
          val result = Bits(32 bits)
          when(Exception){
            result := B(0,32 bits)
          }.elsewhen(!operation_sub_addBar){
            result := output_sign ## sub_diff.asBits
          }.otherwise{
            result := output_sign ## add_sum.asBits
          }
        }

        val mul = new Area{
          val exception = rs1.exp.andR || rs2.exp.andR
          val zero = rs1.isZero | rs2.isZero

          val sign = rs1.sign ^ rs2.sign

          val preTempOut = UInt(48 bits)
          preTempOut := rs1.manextend * rs2.manextend
          val preExp = Mux(preTempOut(47), U(1,1 bits), U(0, 1 bits))
          val tempout = Mux(preExp.msb, preTempOut, preTempOut |<< 1)
          val rounded = tempout(22 downto 0).orR
          val mantissa = tempout(46 downto 24) + ((tempout(23) & rounded).asUInt).resized
          val expSum = UInt(9 bits)
          val tempExp = UInt(9 bits)
          tempExp := (B(0,1 bits) ## rs1.exp.asBits).asUInt + (B(0,1 bits) ## rs2.exp.asBits).asUInt
          expSum := tempExp - U(127,8 bits) + preExp
          val overFlow = ((expSum(8) & !expSum(7)) & !zero)
          val underFlow = Mux(((expSum(8) & expSum(7)) & !zero), Bool(true), Bool(false))
          
          val result = Bits(32 bits)
          when(exception){
            result := B(0,32 bits)
          }.elsewhen(zero){
            result := sign.asBits ## B(0,31 bits)
          }.elsewhen(overFlow){
            result := sign.asBits ## B(255,8 bits) ## B(0,23 bits)
          }.elsewhen(underFlow){
            result := sign.asBits ## B(0,31 bits)
          }.otherwise{
            result := sign.asBits ## expSum(7 downto 0).asBits ## mantissa.asBits
          }
		    }

        when(command.cpuinstruction(26 downto 25) === B"00"){
          response.data := addsub.result
        }.elsewhen(command.cpuinstruction(26 downto 25) === B"01"){
          response.data := addsub.result
        }.elsewhen(command.cpuinstruction(26 downto 25) === B"10"){
          response.data := mul.result
        }.otherwise{
          response.data := (RS1.asUInt + RS2.asUInt).asBits
        }
        
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
