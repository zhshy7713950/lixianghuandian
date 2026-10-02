package com.ruimeng.things.home

import android.graphics.Point
import android.os.Bundle
import android.view.View
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.camera.CameraManager

/** The visual window scrolls, so camera decoding covers the full preview. */
class ScrollingCaptureFragment : CaptureFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screen = Point()
        requireActivity().windowManager.defaultDisplay.getSize(screen)
        CameraManager.FRAME_WIDTH = screen.x
        CameraManager.FRAME_HEIGHT = screen.y
        CameraManager.FRAME_MARGINTOP = 0
    }
}
