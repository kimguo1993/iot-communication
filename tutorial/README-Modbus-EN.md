# ModbusTCP Protocol Tutorial

[HOME BACK](../README.md)

## Communication Connection

- By default, the long connection mode is adopted. You need to close connection manually when it is not in use.
- If a short connection is required, you need to set it manually.

### 1. Long Connection Mode

```java
class Demo {
    public static void main(String[] args) {
        // long connection mode, persistence = true
        ModbusTcp plc = new ModbusTcp(1, "127.0.0.1");
        plc.writeInt16(2, (short) 10);
        short data = plc.readInt16(2);
        // close it manually
        plc.close();
    }
}
```

### 2. Short Connection Mode

```java
class Demo {
    public static void main(String[] args) {
        // short connection mode
        ModbusTcp plc = new ModbusTcp(1, "127.0.0.1");
        // set short connection mode, persistence = false
        plc.setPersistence(false);
        plc.writeInt16(2, (short) 10);
        short data = plc.readInt16(2);
    }
}
```

## Read data

```java
class Demo {
    public static void main(String[] args) {
        ModbusTcp plc = new ModbusTcp(1, "127.0.0.1");

        // read coil
        List<Boolean> readCoil = plc.readCoil(0, 2);

        // read discrete input
        List<Boolean> readDiscreteInput = plc.readDiscreteInput(0, 4);

        // read hold register
        byte[] readHoldRegister = plc.readHoldRegister(0, 4);

        // read input register
        byte[] readInputRegister = plc.readInputRegister(0, 2);

        // hold register read Int16
        short readInt16 = plc.readInt16(2);

        // hold register read UInt16
        int readUInt16 = plc.readUInt16(2);

        // hold register read Int32
        int readInt32 = plc.readInt32(2);

        // hold register read Int32
        long readUInt32 = plc.readUInt32(2);

        // hold register read Float32
        float readFloat32 = plc.readFloat32(2);

        // hold register read Float64
        double readFloat64 = plc.readFloat64(2);

        // hold register read String
        String readString = plc.readString(2, 4);

        plc.close();
    }
}
```

## Write data

```java
class Demo {
    public static void main(String[] args) {
        ModbusTcp plc = new ModbusTcp(1, "127.0.0.1");

        // single write coil
        plc.writeCoil(0, true);

        // multiple write coil
        List<Boolean> booleans = Arrays.asList(true, false, true, false);
        plc.writeCoil(0, booleans);

        // single write hold register
        plc.writeHoldRegister(0, 33);
        // multiple write hold register
        plc.writeHoldRegister(3, new byte[]{(byte) 0x11, (byte) 0x12});
        // multiple write hold register
        List<Integer> integers = Arrays.asList(11, 12, 13, 14);
        plc.writeHoldRegister(3, integers);

        // hold register write int16
        plc.writeInt16(2, (short) 10);

        // hold register write uint16
        plc.writeUInt16(2, 20);

        // hold register write int32
        plc.writeInt32(2, 32);

        // hold register write uint32
        plc.writeUInt32(2, 32L);

        // hold register write float32
        plc.writeFloat32(2, 12.12f);

        // hold register write float64
        plc.writeFloat64(2, 33.21);

        // hold register write String
        plc.writeString(2, "1234");

        plc.close();
    }
}
```