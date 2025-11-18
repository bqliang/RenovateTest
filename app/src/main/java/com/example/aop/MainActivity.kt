package com.example.aop

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    doSomeHeavyWork()
  }

  @LogTime
  private fun doSomeHeavyWork() {
    Thread.sleep(100)
  }
}
