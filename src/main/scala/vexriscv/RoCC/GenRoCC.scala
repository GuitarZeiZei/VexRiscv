/**
  * Created on 2021/2/5
  * 
  */

package vexriscv.RoCC

import spinal.core._
import vexriscv.plugin._
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import vexriscv.{plugin, VexRiscv, VexRiscvConfig, _}

import scala.collection.mutable.ArrayBuffer

case class CoprocessorConfig(cpuPlugins : ArrayBuffer[Plugin[VexRiscv]],
                             cocpuPlugins : ArrayBuffer[Plugin[VexRiscv]]) {

                             }

object CoprocessorConfig {
  def default = CoprocessorConfig(
    cpuPlugins = ArrayBuffer( // Define the processor
      new IBusCachedPlugin(
          prediction = DYNAMIC,
          config = InstructionCacheConfig(
            cacheSize = 4096,
            bytePerLine =32,
            wayCount = 1,
            addressWidth = 32,
            cpuDataWidth = 32,
            memDataWidth = 32,
            catchIllegalAccess = true,
            catchAccessFault = true,
            asyncTagMemory = false,
            twoCycleRam = true,
            twoCycleCache = true
          ),
          memoryTranslatorPortConfig = MmuPortConfig(
            portTlbSize = 4
          )
        ),
        new DBusCachedPlugin(
          config = new DataCacheConfig(
            cacheSize         = 4096,
            bytePerLine       = 32,
            wayCount          = 1,
            addressWidth      = 32,
            cpuDataWidth      = 32,
            memDataWidth      = 32,
            catchAccessError  = true,
            catchIllegal      = true,
            catchUnaligned    = true
          ),
          memoryTranslatorPortConfig = MmuPortConfig(
            portTlbSize = 6
          )
        ),
        new MmuPlugin(
          virtualRange = _(31 downto 28) === 0xC,
          ioRange      = _(31 downto 28) === 0xF
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = true
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = false
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true
        ),
        new FullBarrelShifterPlugin,
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new MulPlugin,
        new DivPlugin,
        new CsrPlugin(CsrPluginConfig.small(0x80000020l)),
        new DebugPlugin(ClockDomain.current.clone(reset = Bool().setName("debugReset"))),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = true
        ),
      new YamlPlugin("cpu0.yaml")
    ),

    cocpuPlugins = ArrayBuffer(
      new RoCCPlugin(new CoProcessorEx1())
    )
  )
}

case class VexRiscvWithRoCC(config: CoprocessorConfig) {
  import config._

  def cpu = new VexRiscv(
    config = VexRiscvConfig(
      plugins = cpuPlugins ++ cocpuPlugins
    )
  )
}

object GenRoCC {
  def main(args: Array[String]): Unit = {
    SpinalVerilog(VexRiscvWithRoCC(CoprocessorConfig.default).cpu)
  }
}
