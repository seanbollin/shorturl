package com.github.tototoshi.base62
/*
 * Copyright 2013 Toshiyuki Takahashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class Base62(baseString: String = ((0 to 9) ++ ('A' to 'Z') ++ ('a' to 'z')).mkString) {

  val base = 62

  def using[A, R <: { def close() }](r: R)(f: R => A): A =
    try { f(r) } finally { r.close() }

  if (baseString.size != base) {
    throw new IllegalArgumentException("baseString length must be %d".format(base))
  }

  def decode(s: String): Long = {
    s.zip(s.indices.reverse)
      .map { case (c, p) => baseString.indexOf(c) * scala.math.pow(base, p).toLong }
      .sum
  }

  def encode(i: Long): String = {

    @scala.annotation.tailrec
    def div(i: Long, res: List[Int] = Nil): List[Int] = {
      (i / base) match {
        case q if q > 0 => div(q, (i % base).toInt :: res)
        case _ => i.toInt :: res
      }
    }

    div(i).map(x => baseString(x)).mkString
  }

}