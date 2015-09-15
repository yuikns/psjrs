package utils.sock

import java.nio.ByteBuffer

/**
 * Created by yu on 9/13/15.
 */
object DataSerializer {

  implicit class LongSerializer(l: Long) {
    def getByteArray: Array[Byte] = ByteBuffer.allocate(8).putLong(l).array()
  }

  implicit class IntSerializer(l: Int) {
    def getByteArray: Array[Byte] = ByteBuffer.allocate(4).putInt(l).array()
  }

  implicit class DoubleSerializer(l: Double) {
    def getByteArray: Array[Byte] = ByteBuffer.allocate(8).putDouble(l).array()
  }

  implicit class CharSerializer(l: Char) {
    def getByteArray: Array[Byte] = ByteBuffer.allocate(2).putChar(l).array()
  }

  implicit class DataUnSerializer(ba: Array[Byte]) {
    def toLong = ByteBuffer.wrap(ba).getLong

    def toInt = ByteBuffer.wrap(ba).getInt

    def toDouble = ByteBuffer.wrap(ba).getDouble

    def toChar = ByteBuffer.wrap(ba).getChar
  }

}
