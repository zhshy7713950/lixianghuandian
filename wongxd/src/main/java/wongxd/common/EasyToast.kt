package wongxd.common

import android.annotation.SuppressLint

import android.content.Context

import android.os.Handler

import android.os.Looper

import android.view.LayoutInflater

import android.widget.TextView

import android.widget.Toast
import wongxd.Wongxd


class EasyToast private constructor(private val builder: Builder) {


    private val context: Context = Wongxd.instance.applicationContext

    private var toast: Toast? = null

    private var tv: TextView? = null

    private var lastTime = 0L


    fun show(resId: Int) {

        show(context.getString(resId))

    }


    fun show(message: String?) {


        if (message?.isBlank() == true) {

            return

        }



        if (System.currentTimeMillis() - lastTime < if (builder.duration == Toast.LENGTH_SHORT) 2000 else 3500) {

            return

        }

        lastTime = System.currentTimeMillis()


        val result = message as String



        if (Looper.myLooper() == Looper.getMainLooper()) {

            showInternal(result)

        } else {

            mainHandler.post { showInternal(result) }

        }


    }


    private fun showInternal(message: String) {


        createToastIfNeeded()



        if (builder.isDefault) {

            toast?.setText(message)

            toast?.show()

        } else {

            tv?.text = message

            toast?.show()

        }


    }


    @SuppressLint("ShowToast")

    private fun createToastIfNeeded() {

        if (toast == null) {

            if (builder.isDefault) {

                toast = Toast.makeText(context, "", builder.duration)

            } else {

                val container = LayoutInflater.from(context).inflate(builder.layoutId, null)

                tv = container.findViewById(builder.tvId)

                toast = Toast(context)

                toast?.view = container

                toast?.duration = builder.duration

            }



            if (builder.gravity != 0) {

                toast?.setGravity(builder.gravity, builder.offsetX, builder.offsetY)

            }

        }

    }


    companion object {


        internal val mainHandler by lazy { return@lazy Handler(Looper.getMainLooper()) }

        /**

         * 默认提供的Toast实例，在首次使用时进行加载。

         */

        val DEFAULT: EasyToast by lazy { return@lazy newBuilder().build() }


        fun newBuilder(): Builder {

            return Builder(true, 0, 0)

        }


        fun newBuilder(layoutId: Int, tvId: Int): Builder {

            return Builder(false, layoutId, tvId)

        }

    }


    class Builder(

        internal var isDefault: Boolean,

        internal var layoutId: Int,

        internal var tvId: Int

    ) {


        internal var duration: Int = Toast.LENGTH_SHORT

        internal var gravity: Int = 0

        internal var offsetX: Int = 0

        internal var offsetY: Int = 0


        fun setGravity(gravity: Int, offsetX: Int, offsetY: Int): Builder {

            this.gravity = gravity

            this.offsetX = offsetX

            this.offsetY = offsetY

            return this

        }


        fun setDuration(duration: Int): Builder {

            this.duration = duration

            return this

        }


        fun build(): EasyToast {

            return EasyToast(this)

        }

    }

}