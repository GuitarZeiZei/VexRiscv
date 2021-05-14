# Li's graduation project 

### This is Li Mengjie's undergraduate graduation project from Southeast University, School of Electronics and Engineering.

Author  | Li Meng-Jie
--------|------------
Mailbox | 213173648@seu.edu.cn

## What this project has done:
- Transplant RoCC coprocessor interface to VexRiscv
- Design a FPU coprocessor
- Verify the design by CPU simulation, SoC simulation and FPGA.


## Index

- [Index](#index)
- [Description](#description)
- [Dependencies](#dependencies)
- [CPU generation](#cpu-generation)
- [RoCC test](#RoCC-test)
- [Interactive debug of the simulated CPU via GDB OpenOCD and Verilator](#interactive-debug-of-the-simulated-cpu-via-gdb-openocd-and-verilator)
- [Murax SoC](#murax-soc)
- [Running Linux](#running-linux)
- [Build the RISC-V GCC](#build-the-risc-v-gcc)



## Description

This repository is Li's graduation project and hosts a RISC-V implementation with instruction extension written in SpinalHDL. Here are some specs :

- RV32I[M][C][A] instruction set (Atomic only inside a single core)
- Pipelined from 2 to 5+ stages ([Fetch*X], Decode, Execute, [Memory], [WriteBack])
- 1.44 DMIPS/Mhz --no-inline when nearly all features are enabled (1.57 DMIPS/Mhz when the divider lookup table is enabled)
- Optimized for FPGA, does not use any vendor specific IP block / primitive
- AXI4, Avalon, wishbone ready
- Optional MUL/DIV extensions
- Optional instruction and data caches
- Optional hardware refilled MMU
- Optional debug extension allowing Eclipse debugging via a GDB >> openOCD >> JTAG connection
- Optional interrupts and exception handling with Machine, [Supervisor] and [User] modes as defined in the [RISC-V Privileged ISA Specification v1.10](https://riscv.org/specifications/privileged-isa/).
- Two implementations of shift instructions: single cycle (full barrel shifter) and shiftNumber cycles
- Each stage can have optional bypass or interlock hazard logic
- Linux compatible (SoC : https://github.com/enjoy-digital/linux-on-litex-vexriscv)
- Zephyr compatible
- [FreeRTOS port](https://github.com/Dolu1990/FreeRTOS-RISCV)

The hardware description of this CPU is done by using a very software oriented approach
(without any overhead in the generated hardware). Here is a list of software concepts used:

- There are very few fixed things. Nearly everything is plugin based. The PC manager is a plugin, the register file is a plugin, the hazard controller is a plugin, ...
- There is an automatic a tool which allows plugins to insert data in the pipeline at a given stage, and allows other plugins to read it in another stage through automatic pipelining.
- There is a service system which provides a very dynamic framework. For instance, a plugin could provide an exception service which can then be used by other plugins to emit exceptions from the pipeline.



## Dependencies

On Ubuntu 18:

```sh
# JAVA JDK 8
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get install openjdk-8-jdk -y
sudo update-alternatives --config java
sudo update-alternatives --config javac

# Install SBT - https://www.scala-sbt.org/
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt

# Verilator (for sim only, really needs 3.9+, in general apt-get will give you 3.8)
sudo apt-get install git make autoconf g++ flex bison
git clone http://git.veripool.org/git/verilator   # Only first time
unsetenv VERILATOR_ROOT  # For csh; ignore error if on bash
unset VERILATOR_ROOT  # For bash
cd verilator
git pull        # Make sure we're up-to-date
git checkout v3.916
autoconf        # Create ./configure script
./configure
make
sudo make install
```

## CPU generation
You can find the example CPU instance in:
- `src/main/scala/vexriscv/RoCC/GenRoCC.scala`

To generate the corresponding RTL with RoCC extension as a `VexRiscv.v` file, run the following command in the root directory of this repository:

```sh
sbt "runMain vexriscv.RoCC.GenRoCC"
```


NOTES:
- It could take time the first time you run it.
- The VexRiscv project may need an unreleased master-head of the SpinalHDL repo. If it fails to compile, just get the SpinalHDL repository and
   do a "sbt clean compile publish-local" in it as described in the dependencies chapter.

## RoCC test
To test RoCC instruction on VexRiscv core, run the following command in the root directory of this repository:

```sh
cd src/test/cpp/custom/rocc/
make

#In the root of VexRiscv repository
sbt "runMain vexriscv.RoCC.GenRoCC"
cd src/test/cpp/RoCCTest/
make clean run CSR=no MMU=no DEBUG_PLUGIN=no MUL=no DIV=no DHRYSTONE=no REDO=2 CUSTOM_ROCC=yes TRACE=yes
```
Then you can find the simulation wave in:

- `src/test/cpp/RoCCTest/custom_rocc.vcd`


To test RoCC instruction on Murax SoC, run the following command in the root directory of this repository:
```sh
cd src/main/c/murax/hello_world/
make

#In the root of VexRiscv repository
sbt "runMain vexriscv.RoCC.MuraxWithRamInit"
```

Then create a vivodo project and program the bitstream onto FPGA board, then some information should be printed to the uart output.



## Interactive debug of the simulated CPU via GDB OpenOCD and Verilator
To use this, you just need to use the same command as with running tests, but adding `DEBUG_PLUGIN_EXTERNAL=yes` in the make arguments.
This works for the `GenFull` configuration, but not for `GenSmallest`, as this configuration has no debug module.

Then, you can use the [OpenOCD RISC-V](https://github.com/SpinalHDL/openocd_riscv) tool to create a GDB server connected to the target (the simulated CPU), as follows:

```sh
#In the VexRiscv repository, to run the simulation on which one OpenOCD can connect itself =>
sbt "runMain vexriscv.demo.GenFull"
cd src/test/cpp/regression
make run DEBUG_PLUGIN_EXTERNAL=yes

#In the openocd git, after building it =>
src/openocd -c "set VEXRISCV_YAML PATH_TO_THE_GENERATED_CPU0_YAML_FILE" -f tcl/target/vexriscv_sim.cfg

#Run a GDB session with an elf RISCV executable (GenFull CPU)
YourRiscvToolsPath/bin/riscv32-unknown-elf-gdb VexRiscvRepo/src/test/resources/elf/uart.elf
target remote localhost:3333
monitor reset halt
load
continue

# Now it should print messages in the Verilator simulation of the CPU
```


## Murax SoC

Murax is a very light SoC (it fits in an ICE40 FPGA) which can work without any external components:
- VexRiscv RV32I[M]
- JTAG debugger (Eclipse/GDB/openocd ready)
- 8 kB of on-chip ram
- Interrupt support
- APB bus for peripherals
- 32 GPIO pin
- one 16 bits prescaler, two 16 bits timers
- one UART with tx/rx fifo

![Briey SoC](assets/Murax.svg?raw=true "")


Its implementation can be found here: `src/main/scala/vexriscv/demo/Murax.scala`.

To generate the Murax SoC Hardware:

```sh
# To generate the SoC without any content in the ram
sbt "runMain vexriscv.demo.Murax"

# To generate the SoC with a demo program already in ram
sbt "runMain vexriscv.demo.MuraxWithRamInit"
```

The demo program included by default with `MuraxWithRamInit` will blink the
LEDs and echo characters received on the UART back to the user. To see this
when running the Verilator sim, type some text and press enter.

Then go in `src/test/cpp/murax` and run the simulation with:

```sh
make clean run
```

To connect OpenOCD (https://github.com/SpinalHDL/openocd_riscv) to the simulation:

```sh
src/openocd -f tcl/interface/jtag_tcp.cfg -c "set MURAX_CPU0_YAML /home/spinalvm/Spinal/VexRiscv/cpu0.yaml" -f tcl/target/murax.cfg
```


Some scripts to generate the SoC and call the icestorm toolchain can be found here: `scripts/Murax/`

A top level simulation testbench with the same features + a GUI are implemented with SpinalSim. You can find it in `src/test/scala/vexriscv/MuraxSim.scala`.

To run it :

```sh
# This will generate the Murax RTL + run its testbench. You need Verilator 3.9xx installated.
sbt "test:runMain vexriscv.MuraxSim"
```


## Build the RISC-V GCC

A prebuild GCC toolsuite can be found here:

- https://www.sifive.com/software/  => Prebuilt RISC‑V GCC Toolchain and Emulator

The VexRiscvSocSoftware makefiles are expecting to find this prebuild version in /opt/riscv/__contentOfThisPreBuild__

```sh
wget https://static.dev.sifive.com/dev-tools/riscv64-unknown-elf-gcc-20171231-x86_64-linux-centos6.tar.gz
tar -xzvf riscv64-unknown-elf-gcc-20171231-x86_64-linux-centos6.tar.gz
sudo mv riscv64-unknown-elf-gcc-20171231-x86_64-linux-centos6 /opt/riscv64-unknown-elf-gcc-20171231-x86_64-linux-centos6
sudo mv /opt/riscv64-unknown-elf-gcc-20171231-x86_64-linux-centos6 /opt/riscv
echo 'export PATH=/opt/riscv/bin:$PATH' >> ~/.bashrc
```

If you want to compile the rv32i and rv32im GCC toolchain from source code and install them in `/opt/`, do the following (will take one hour):

```sh
# Be carefull, sometime the git clone has issue to successfully clone riscv-gnu-toolchain.
sudo apt-get install autoconf automake autotools-dev curl libmpc-dev libmpfr-dev libgmp-dev gawk build-essential bison flex texinfo gperf libtool patchutils bc zlib1g-dev -y

git clone --recursive https://github.com/riscv/riscv-gnu-toolchain riscv-gnu-toolchain
cd riscv-gnu-toolchain

echo "Starting RISC-V Toolchain build process"

ARCH=rv32im
rmdir -rf $ARCH
mkdir $ARCH; cd $ARCH
../configure  --prefix=/opt/$ARCH --with-arch=$ARCH --with-abi=ilp32
sudo make -j4
cd ..


ARCH=rv32i
rmdir -rf $ARCH
mkdir $ARCH; cd $ARCH
../configure  --prefix=/opt/$ARCH --with-arch=$ARCH --with-abi=ilp32
sudo make -j4
cd ..

echo -e "\\nRISC-V Toolchain installation completed!"
```

