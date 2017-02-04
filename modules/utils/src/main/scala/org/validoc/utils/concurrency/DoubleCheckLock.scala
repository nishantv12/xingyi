package org.validoc.utils.concurrency


class DoubleCheckLock {
  def apply(condition: => Boolean)(block: => Any): Unit = {
    if (condition) {
      this.synchronized {
        if (condition) block
      }
    }
  }
}

