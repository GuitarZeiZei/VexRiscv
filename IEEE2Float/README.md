# IEEE2eFloat
## 该文件夹包含处理串口数据的Python代码以及串口数据导出的txt文件，用于验证FPU运算是否正确

### RoCC FPU协处理器通过串口发送数据，数据为满足IEEE 754格式的8位十六进制数，将串口数据保存在txt文件中以备处理。
注意：txt中数据应满足一定的格式保存。示例如下:  

```
op1,operator,op2,=,result    
op1,operator,op2,=,result  
```

### IEEE2Float.py将十六进制数的浮点数转为十进制浮点数，并验算FPU的运算是否正确。若计算精度小于1e-4，则在将该运算的标签置为True，否则置为False。