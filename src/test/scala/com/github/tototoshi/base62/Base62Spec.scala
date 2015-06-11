/*
 * Copyright 2015, by Sean Bollin.
 *
 * Simple test class for a Base62 convert.
 *
 */

import org.scalatest._
import com.github.tototoshi.base62._

class Base62Spec extends FlatSpec with Matchers {
  it should "convert 1 to 1" in {
    val base62 = new Base62
    val encoded = base62.encode(1)

    encoded shouldBe "1"
  }

  it should "convert 12432 to 3EW" in {
    val base62 = new Base62
    val encoded = base62.encode(12432)

    encoded shouldBe "3EW"
  }
}
