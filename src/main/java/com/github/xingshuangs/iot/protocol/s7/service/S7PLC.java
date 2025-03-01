package com.github.xingshuangs.iot.protocol.s7.service;


import com.github.xingshuangs.iot.exceptions.S7CommException;
import com.github.xingshuangs.iot.protocol.s7.enums.EArea;
import com.github.xingshuangs.iot.protocol.s7.enums.EDataVariableType;
import com.github.xingshuangs.iot.protocol.s7.enums.EParamVariableType;
import com.github.xingshuangs.iot.protocol.s7.enums.EPlcType;
import com.github.xingshuangs.iot.protocol.s7.model.DataItem;
import com.github.xingshuangs.iot.protocol.s7.model.RequestItem;
import com.github.xingshuangs.iot.protocol.s7.model.S7Data;
import com.github.xingshuangs.iot.protocol.s7.utils.AddressUtil;
import com.github.xingshuangs.iot.utils.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xingshuang
 */
public class S7PLC extends PLCNetwork {

    public static final int PORT = 102;

    public static final int DEFAULT_PDU_LENGTH = 240;

    public static final String IP = "127.0.0.1";

    public S7PLC() {
        this(EPlcType.S1200, IP, PORT, 0, 0, DEFAULT_PDU_LENGTH);
    }

    public S7PLC(EPlcType plcType) {
        this(plcType, IP, PORT, 0, 0, DEFAULT_PDU_LENGTH);
    }

    public S7PLC(EPlcType plcType, String ip) {
        this(plcType, ip, PORT, 0, 0, DEFAULT_PDU_LENGTH);
    }

    public S7PLC(EPlcType plcType, String ip, int port, int rack, int slot) {
        this(plcType, ip, port, rack, slot, DEFAULT_PDU_LENGTH);
    }

    public S7PLC(EPlcType plcType, String ip, int port, int rack, int slot, int pduLength) {
        super(ip, port);
        this.plcType = plcType;
        this.rack = rack;
        this.slot = slot;
        this.pduLength = pduLength;
    }

    //region 读取数据

    /**
     * 最原始的方式读取生数据
     *
     * @param variableType 参数类型
     * @param count        数据个数
     * @param area         区域
     * @param dbNumber     DB块编号
     * @param byteAddress  字节地址
     * @param bitAddress   位地址
     * @return 字节数组
     */
    public byte[] readRaw(EParamVariableType variableType, int count, EArea area, int dbNumber, int byteAddress, int bitAddress) {
        if (count <= 0) {
            throw new IllegalArgumentException("count<=0");
        }
        if (dbNumber < 0) {
            throw new IllegalArgumentException("dbNumber<0");
        }
        if (byteAddress < 0) {
            throw new IllegalArgumentException("byteAddress<0");
        }
        if (bitAddress < 0 || bitAddress > 7) {
            throw new IllegalArgumentException("bitAddress<0||bitAddress>7");
        }
        RequestItem requestItem = RequestItem.createByParams(variableType, count, area, dbNumber, byteAddress, bitAddress);
        DataItem dataItem = this.readS7Data(requestItem);
        return dataItem.getData();
    }

    /**
     * 多地址读取字节数据
     *
     * @param addressRead 地址包装列表
     * @return 字节数组列表
     */
    public List<byte[]> readMultiByte(MultiAddressRead addressRead) {
        List<DataItem> dataItems = this.readS7Data(addressRead.getRequestItems());
        return dataItems.stream().map(DataItem::getData).collect(Collectors.toList());
    }

    /**
     * 单地址字节数据读取
     *
     * @param address 地址
     * @param count   字节个数
     * @return 字节数组
     */
    public byte[] readByte(String address, int count) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, count));
        return dataItem.getData();
    }

    /**
     * 读取一个字节
     *
     * @param address 地址
     * @return 一个字节
     */
    public byte readByte(String address) {
        return this.readByte(address, 1)[0];
    }

    /**
     * 读取一个boolean
     *
     * @param address 地址
     * @return 一个boolean值
     */
    public boolean readBoolean(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseBit(address));
        return BooleanUtil.getValue(dataItem.getData()[0], 0);
    }

    /**
     * 读取多个个boolean值
     *
     * @param address 地址
     * @return 多个boolean值
     */
    public List<Boolean> readBoolean(String... address) {
        return this.readBoolean(Arrays.asList(address));
    }

    /**
     * 读取boolean列表
     *
     * @param addresses 地址列表
     * @return boolean列表
     */
    public List<Boolean> readBoolean(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(AddressUtil::parseBit).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> BooleanUtil.getValue(x.getData()[0], 0)).collect(Collectors.toList());
    }

    /**
     * 读取一个Int16 2字节数据
     *
     * @param address 地址
     * @return 一个Int16 2字节数据
     */
    public short readInt16(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 2));
        return ShortUtil.toInt16(dataItem.getData());
    }

    /**
     * 读取Int16 2字节数据列表
     *
     * @param address 地址
     * @return Int16 2字节数据列表
     */
    public List<Short> readInt16(String... address) {
        return this.readInt16(Arrays.asList(address));
    }

    /**
     * 读取Int16 2字节数据列表
     *
     * @param addresses 地址列表
     * @return Int16 2字节数据列表
     */
    public List<Short> readInt16(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(x -> AddressUtil.parseByte(x, 2)).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> ShortUtil.toInt16(x.getData())).collect(Collectors.toList());
    }

    /**
     * 读取一个UInt16 2字节数据
     *
     * @param address 地址
     * @return 一个UInt16 2字节数据
     */
    public int readUInt16(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 2));
        return ShortUtil.toUInt16(dataItem.getData());
    }

    /**
     * 读取UInt16 2字节数据列表
     *
     * @param address 地址
     * @return UInt16 2字节数据列表
     */
    public List<Integer> readUInt16(String... address) {
        return this.readUInt16(Arrays.asList(address));
    }

    /**
     * 读取UInt16 2字节数据列表
     *
     * @param addresses 地址列表
     * @return UInt16 2字节数据列表
     */
    public List<Integer> readUInt16(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(x -> AddressUtil.parseByte(x, 2)).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> ShortUtil.toUInt16(x.getData())).collect(Collectors.toList());
    }

    /**
     * 读取一个UInt32 4字节数据
     *
     * @param address 地址
     * @return 一个UInt32 4字节数据
     */
    public int readInt32(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 4));
        return IntegerUtil.toInt32(dataItem.getData());
    }

    /**
     * 读取UInt32 4字节数据列表
     *
     * @param address 地址
     * @return UInt32 4字节数据列表
     */
    public List<Integer> readInt32(String... address) {
        return this.readInt32(Arrays.asList(address));
    }

    /**
     * 读取UInt32 4字节数据列表
     *
     * @param addresses 地址列表
     * @return UInt32 4字节数据列表
     */
    public List<Integer> readInt32(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(x -> AddressUtil.parseByte(x, 4)).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> IntegerUtil.toInt32(x.getData())).collect(Collectors.toList());
    }

    /**
     * 读取一个UInt32 4字节数据
     *
     * @param address 地址
     * @return 一个UInt32 4字节数据
     */
    public long readUInt32(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 4));
        return IntegerUtil.toUInt32(dataItem.getData());
    }

    /**
     * 读取UInt32 4字节数据列表
     *
     * @param address 地址
     * @return UInt32 4字节数据列表
     */
    public List<Long> readUInt32(String... address) {
        return this.readUInt32(Arrays.asList(address));
    }

    /**
     * 读取UInt32 4字节数据列表
     *
     * @param addresses 地址列表
     * @return UInt32 4字节数据列表
     */
    public List<Long> readUInt32(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(x -> AddressUtil.parseByte(x, 4)).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> IntegerUtil.toUInt32(x.getData())).collect(Collectors.toList());
    }

    /**
     * 读取一个Float32的数据
     *
     * @param address 地址
     * @return 一个Float32的数据
     */
    public float readFloat32(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 4));
        return FloatUtil.toFloat32(dataItem.getData());
    }

    /**
     * 读取多个Float32的数据
     *
     * @param address 地址
     * @return 多个Float32的数据
     */
    public List<Float> readFloat32(String... address) {
        return this.readFloat32(Arrays.asList(address));
    }

    /**
     * 读取多个Float32的数据
     *
     * @param addresses 地址列表
     * @return 多个Float32的数据
     */
    public List<Float> readFloat32(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(x -> AddressUtil.parseByte(x, 4)).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> FloatUtil.toFloat32(x.getData())).collect(Collectors.toList());
    }

    /**
     * 读取一个Float64的数据
     *
     * @param address 地址
     * @return 一个Float64的数据
     */
    public double readFloat64(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 8));
        return FloatUtil.toFloat64(dataItem.getData());
    }

    /**
     * 读取多个Float64的数据
     *
     * @param address 多个地址
     * @return 多个Float64的数据
     */
    public List<Double> readFloat64(String... address) {
        return this.readFloat64(Arrays.asList(address));
    }

    /**
     * 读取多个Float64的数据
     *
     * @param addresses 地址列表
     * @return 多个Float64的数据
     */
    public List<Double> readFloat64(List<String> addresses) {
        List<RequestItem> requestItems = addresses.stream().map(x -> AddressUtil.parseByte(x, 8)).collect(Collectors.toList());
        List<DataItem> dataItems = this.readS7Data(requestItems);
        return dataItems.stream().map(x -> FloatUtil.toFloat64(x.getData())).collect(Collectors.toList());
    }

    /**
     * 读取字符串
     * String（字符串）数据类型存储一串单字节字符，
     * String提供了多大256个字节，前两个字节分别表示字节中最大的字符数和当前的字符数，定义字符串的最大长度可以减少它的占用存储空间
     *
     * @param address 地址
     * @return 字符串
     */
    public String readString(String address) {
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 2));
        int type = ByteUtil.toUInt8(dataItem.getData(), 0);
        if (type == 0 || type == 255) {
            throw new S7CommException("该地址的值不是字符串类型");
        }
        int length = ByteUtil.toUInt8(dataItem.getData(), 1);
        dataItem = this.readS7Data(AddressUtil.parseByte(address, 2 + length));
        return ByteUtil.toStr(dataItem.getData(), 2);
    }

    /**
     * 读取字符串
     *
     * @param address 地址
     * @param length  字符串长度
     * @return 字符串
     */
    public String readString(String address, int length) {
        if (length <= 0 || length > 254) {
            throw new IllegalArgumentException("length <= 0 || length > 254");
        }
        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 2 + length));
        int type = ByteUtil.toUInt8(dataItem.getData(), 0);
        if (type == 0 || type == 255) {
            throw new S7CommException("该地址的值不是字符串类型");
        }
        int actLength = ByteUtil.toUInt8(dataItem.getData(), 1);
        return ByteUtil.toStr(dataItem.getData(), 2, Math.min(actLength, length));
    }

//    /**
//     * 读取字符串
//     * Wsting数据类型与sting数据类型接近，支持单字值的较长字符串，
//     * 第一个字包含最大总字符数，下一个字包含的是当前的总字符数，接下来的字符串可含最多65534个字
//     *
//     * @param address 地址
//     * @return 字符串
//     */
//    public String readWString(String address) {
//        DataItem dataItem = this.readS7Data(AddressUtil.parseByte(address, 4));
//        int type = ShortUtil.toUInt16(dataItem.getData());
//        if (type == 0 || type == 65535) {
//            throw new S7CommException("该地址的值不是字符串WString类型");
//        }
//        int length = ShortUtil.toUInt16(dataItem.getData(), 2);
//        dataItem = this.readS7Data(AddressUtil.parseByte(address, 4 + length * 2));
//        return ByteUtil.toStr(dataItem.getData(), 4);
//    }

    /**
     * 读取时间，时间为毫秒时间，ms，例如1000ms
     *
     * @param address 地址
     * @return 时间，ms
     */
    public long readTime(String address) {
        return this.readUInt32(address);
    }

    /**
     * 读取日期，例如：2023-04-04
     *
     * @param address 地址
     * @return 日期
     */
    public LocalDate readDate(String address) {
        int offset = this.readUInt16(address);
        return LocalDate.of(1990, 1, 1).plusDays(offset);
    }

    /**
     * 读取一天中的时间，例如：23:56:31
     *
     * @param address 地址
     * @return 时间
     */
    public LocalTime readTimeOfDay(String address) {
        long value = this.readUInt32(address);
        return LocalTime.ofSecondOfDay(value / 1000);
    }

    //endregion

    //region 写入数据

    /**
     * 最原始的方式写入生数据
     *
     * @param variableType     参数类型
     * @param count            数据个数
     * @param area             区域
     * @param dbNumber         DB块编号
     * @param byteAddress      字节地址
     * @param bitAddress       位地址
     * @param dataVariableType 数据变量类型
     * @param data             数据字节数组
     */
    public void writeRaw(EParamVariableType variableType, int count, EArea area, int dbNumber, int byteAddress,
                         int bitAddress, EDataVariableType dataVariableType, byte[] data) {
        if (count <= 0) {
            throw new IllegalArgumentException("count<=0");
        }
        if (dbNumber <= 0) {
            throw new IllegalArgumentException("dbNumber<=0");
        }
        if (byteAddress < 0) {
            throw new IllegalArgumentException("byteAddress<0");
        }
        if (bitAddress < 0 || bitAddress > 7) {
            throw new IllegalArgumentException("bitAddress<0||bitAddress>7");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data");
        }
        RequestItem requestItem = RequestItem.createByParams(variableType, count, area, dbNumber, byteAddress, bitAddress);
        DataItem dataItem = DataItem.createReq(data, dataVariableType);
        this.writeS7Data(requestItem, dataItem);
    }

    /**
     * 写入boolean数据
     *
     * @param address 地址
     * @param data    boolean数据
     */
    public void writeBoolean(String address, boolean data) {
        this.writeS7Data(AddressUtil.parseBit(address), DataItem.createReqByBoolean(data));
    }

    /**
     * 写入字节数据
     *
     * @param address 地址
     * @param data    字节数据
     */
    public void writeByte(String address, byte data) {
        this.writeS7Data(AddressUtil.parseByte(address, 1), DataItem.createReqByByte(data));
    }

    /**
     * 写入字节列表数据
     *
     * @param address 地址
     * @param data    字节列表数据
     */
    public void writeByte(String address, byte[] data) {
        this.writeS7Data(AddressUtil.parseByte(address, data.length), DataItem.createReqByByte(data));
    }

    /**
     * 写入UInt16数据
     *
     * @param address 地址
     * @param data    UInt16数据
     */
    public void writeUInt16(String address, int data) {
        this.writeByte(address, ShortUtil.toByteArray(data));
    }

    /**
     * 写入Int16数据
     *
     * @param address 地址
     * @param data    Int16数据
     */
    public void writeInt16(String address, short data) {
        this.writeByte(address, ShortUtil.toByteArray(data));
    }

    /**
     * 写入UInt32数据
     *
     * @param address 地址
     * @param data    UInt32数据
     */
    public void writeUInt32(String address, long data) {
        this.writeByte(address, IntegerUtil.toByteArray(data));
    }

    /**
     * 写入Int32数据
     *
     * @param address 地址
     * @param data    Int32数据
     */
    public void writeInt32(String address, int data) {
        this.writeByte(address, IntegerUtil.toByteArray(data));
    }

    /**
     * 写入Float32数据
     *
     * @param address 地址
     * @param data    Float32数据
     */
    public void writeFloat32(String address, float data) {
        this.writeByte(address, FloatUtil.toByteArray(data));
    }

    /**
     * 写入Float64数据
     *
     * @param address 地址
     * @param data    Float64数据
     */
    public void writeFloat64(String address, double data) {
        this.writeByte(address, FloatUtil.toByteArray(data));
    }

    /**
     * 多地址写入数据
     *
     * @param addressWrite 数据
     */
    public void writeMultiData(MultiAddressWrite addressWrite) {
        this.writeS7Data(addressWrite.getRequestItems(), addressWrite.getDataItems());
    }

    /**
     * 写入字符串数据
     * String（字符串）数据类型存储一串单字节字符，
     * String提供了多大256个字节，前两个字节分别表示字节中最大的字符数和当前的字符数，定义字符串的最大长度可以减少它的占用存储空间
     *
     * @param address 地址
     * @param data    字符串数据
     */
    public void writeString(String address, String data) {
        if (data.length() > 254) {
            throw new IllegalArgumentException("data字符串参数过长，超过254");
        }
        byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);
        byte[] tmp = new byte[2 + dataBytes.length];
        tmp[0] = (byte) 0xFE;
        tmp[1] = ByteUtil.toByte(dataBytes.length);
        System.arraycopy(dataBytes, 0, tmp, 2, dataBytes.length);
        this.writeByte(address, tmp);
    }

//    /**
//     * 写入字符串数据
//     * Wsting数据类型与sting数据类型接近，支持单字值的较长字符串，
//     * 第一个字包含最大总字符数，下一个字包含的是当前的总字符数，接下来的字符串可含最多65534个字
//     *
//     * @param address 地址
//     * @param data    字符串数据
//     */
//    public void writeWString(String address, String data) {
//        if (data.length() > (65534*2-4)) {
//            throw new IllegalArgumentException("data字符串参数过长");
//        }
//        byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);
//        byte[] tmp = new byte[2 + dataBytes.length];
//        byte[] lengthBytes = ShortUtil.toByteArray(dataBytes.length / 2);
//        tmp[0] = (byte) 0xFF;
//        tmp[1] = (byte) 0xFE;
//        tmp[2] = lengthBytes[0];
//        tmp[3] = lengthBytes[1];
//        System.arraycopy(dataBytes, 0, tmp, 4, dataBytes.length);
//        this.writeByte(address, tmp);
//    }

    /**
     * 写入时间，时间为毫秒时间，ms
     *
     * @param address 地址
     * @param time    时间，ms
     */
    public void writeTime(String address, long time) {
        this.writeUInt32(address, time);
    }

    /**
     * 读取日期
     *
     * @param address 地址
     * @param date    日期
     */
    public void writeDate(String address, LocalDate date) {
        LocalDate start = LocalDate.of(1990, 1, 1);
        long value = date.toEpochDay() - start.toEpochDay();
        this.writeUInt16(address, (int) value);
    }

    /**
     * 写入一天中的时间
     *
     * @param address 地址
     * @param time    时间
     */
    public void writeTimeOfDay(String address, LocalTime time) {
        int value = time.toSecondOfDay();
        this.writeUInt32(address, (long) value * 1000);
    }

    //endregion

    //region 控制部分

    /**
     * 热重启
     */
    public void hotRestart() {
        this.readFromServerWithPersistence(S7Data.createHotRestart());
    }

    /**
     * 冷重启
     */
    public void coldRestart() {
        this.readFromServerWithPersistence(S7Data.createColdRestart());
    }

    /**
     * PLC停止
     */
    public void plcStop() {
        this.readFromServerWithPersistence(S7Data.createPlcStop());
    }

    /**
     * 将ram复制到rom
     */
    public void copyRamToRom() {
        this.readFromServerWithPersistence(S7Data.createCopyRamToRom());
    }

    /**
     * 压缩
     */
    public void compress() {
        this.readFromServerWithPersistence(S7Data.createCompress());
    }

    //endregion
}
