/** Copyright (c) 2018, Phil Rice. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS AS IS AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package one.xingyi.core.strings

import java.io.{ByteArrayOutputStream, PrintStream}

import one.xingyi.core.metrics.PrintlnPutMetrics

object Strings {
  def classNameOfObject(obj: Object): String = obj.getClass.getSimpleName.dropRight(1)
  def indent(filler: String, depth: Int): String = List.fill(depth)(filler).mkString("")

  def indentTuple(filler: String, left: Int, mid: Int)(tuple: (String, String)) = {
    val padding = mid - left - tuple._1.length
    indent(filler, left) + tuple._1 + indent(filler, padding) + tuple._2
  }


  def removeWhiteSpace(s: String): String = s.replaceAll("\\s+", "")

  def ellipses(maxLength: Int)(s: String): String = if (s.length > maxLength) s.take(maxLength) + ".." else s

  def lastSection(marker: String)(s: String) = s.split(marker).last
  def allButlastSection(marker: String)(s: String) = s.split(marker).dropRight(1).mkString(marker)
  def recordPrintln[X](x: => X): (X, String) = {
    val bytes = new ByteArrayOutputStream()
    val result = Console.withOut(new PrintStream(bytes))(x)
    (result, bytes.toString("UTF-8"))
  }

  def trimChar(trim: Char)(s: String) = s.dropWhile(_ == trim).reverse.dropWhile(_ == trim).reverse


  def cleanString(s: String, acceptedChars: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_- ") = s.filter(acceptedChars.contains(_)).mkString

  def uri(parts: String*): String = parts.map(trimChar('/')).mkString("/")

}

