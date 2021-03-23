//#include "stddefs.h"
#include <stdint.h>

#include "murax.h"

inline int RoCC(int rs1, int rs2){
	int RoCC_result;
	asm volatile(
		".insn r 0x0b,6,1,%0,%1,%2"
		:"=r"(RoCC_result)
		:"r"(rs1),"r"(rs2)
	);
	return RoCC_result;
}

void print(const char*str){
	while(*str){
		uart_write(UART,*str);
		str++;
	}
}
void println(const char*str){
	print(str);
	uart_write(UART,'\n');
}

void delay(uint32_t loops){
	for(int i=0;i<loops;i++){
		int tmp = GPIO_A->OUTPUT;
	}
}

void main() {
    GPIO_A->OUTPUT_ENABLE = 0x0000000F;
	GPIO_A->OUTPUT = 0x00000001;
    println("hello world arty a7 v1");
    const int nleds = 4;
	const int nloops = 2000000;

	/************RoCC TEST***********/
	int a = 1, b = 2;
	int c = RoCC(a, b);
	println("RoCC executed");
	if(c == 3) 
		println("RoCC TEST SUCCESS!");
	else 
		println("RoCC TEST FAIL!");

    while(1){
    	for(unsigned int i=0;i<nleds-1;i++){
    		GPIO_A->OUTPUT = 1<<i;
    		delay(nloops);
    	}
    	for(unsigned int i=0;i<nleds-1;i++){
			GPIO_A->OUTPUT = (1<<(nleds-1))>>i;
			delay(nloops);
		}
    }
}

void irqCallback(){
}
