package net.crispywalrus

package object socku {

  val chars: Array[Char] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toArray

  trait ConvertBase {
    def base: BigInt
    def toChar(digit: Int) = {
      assert(digit >= 0)
      assert(digit <= base)
      chars(digit)
    }

    def convert(bi: BigInt): Array[Char] = {
      if (bi == 0) Array()
      else {
        val (div, rem: BigInt) = bi /% base
        convert(div) :+ toChar(rem.toInt)
      }
    }
  }

  object base62 extends ConvertBase {
    val base = BigInt(62)
    def apply(value: BigInt): String = new String(convert(value))
  }

  object base36 extends ConvertBase {
    val base = BigInt(36)
    def apply(value: BigInt): String = new String(convert(value))
  }
  
}


