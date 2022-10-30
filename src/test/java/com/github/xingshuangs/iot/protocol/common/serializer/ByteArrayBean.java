package com.github.xingshuangs.iot.protocol.common.serializer;


import com.github.xingshuangs.iot.protocol.common.enums.EDataType;
import lombok.Data;

/**
 * @author xingshuang
 */
@Data
public class ByteArrayBean {

    @ByteArrayVariable(byteOffset = 0, bitOffset = 0, count = 1, type = EDataType.BOOL)
    Boolean boolData;

    @ByteArrayVariable(byteOffset = 0, count = 1, type = EDataType.BYTE)
    Byte byteData;

    @ByteArrayVariable(byteOffset = 3, count = 1, type = EDataType.UINT16)
    Integer uint16Data;

    @ByteArrayVariable(byteOffset = 3, count = 1, type = EDataType.INT16)
    Short int16Data;

    @ByteArrayVariable(byteOffset = 5, count = 1, type = EDataType.UINT32)
    Long uint32Data;

    @ByteArrayVariable(byteOffset = 9, count = 1, type = EDataType.INT32)
    Integer int32Data;

    @ByteArrayVariable(byteOffset = 13, count = 1, type = EDataType.FLOAT32)
    Float float32Data;

    @ByteArrayVariable(byteOffset = 21, count = 1, type = EDataType.FLOAT64)
    Double float64Data;

    @ByteArrayVariable(byteOffset = 37, count = 3, type = EDataType.STRING)
    String stringData;
}
