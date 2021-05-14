"""
    Created on 2021/4/24
    Read hex data in IEEE 754 format and transform to float in decimal.
    Verify whether floating point calculation is correct.
"""
import struct
import ctypes
import pandas as pd

def hex_to_float(h):
    i = int(h,16)
    return struct.unpack('<f',struct.pack('<I', i))[0]

def hex2float(h):
    i = int(h,16)
    cp = ctypes.pointer(ctypes.c_int(i))
    fp = ctypes.cast(cp,ctypes.POINTER(ctypes.c_float))
    return fp.contents.value

data = pd.read_csv('data.txt', names=['op1','','op2', '=','result'])

print("orogin data read from UART:")
print(data.iloc[0:20])

for i in range(20):
    for j in range(3):
        data.loc[i][j*2] = hex2float(data.loc[i][j*2])
    
data.insert(5,'label',0)
limit = 1e-4
for i in range(5):
    error = abs(data.iloc[i,0] + data.iloc[i,2] - data.iloc[i,4])
    data.iloc[i,5] = error<limit
for i in range(5,10):
    error = abs(data.iloc[i,0] - data.iloc[i,2] - data.iloc[i,4])
    data.iloc[i,5] = error<limit
for i in range(10,15):
    error = abs(data.iloc[i,0] * data.iloc[i,2] - data.iloc[i,4])
    data.iloc[i,5] = error<limit
for i in range(15,20):
    error = abs(data.iloc[i,0] / data.iloc[i,2] - data.iloc[i,4])
    data.iloc[i,5] = error<limit

print("")
print("Data processed:")
print(data.iloc[0:20])