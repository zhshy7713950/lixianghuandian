
@file:Suppress("unused")

package com.skydoves.only

import android.view.View

/** View visibility [Only] extension.  */
fun View.onlyVisibility(name: String, times: Int, visible: Boolean) {
  val view = this
  only(name, times) {
    onDo { view.visible(visible) }
    onDone { view.visible(!visible) }
  }
}

internal fun View.visible(visible: Boolean) {
  if (visible) this.visibility = View.VISIBLE
  else this.visibility = View.GONE
}
