package wongxd.base.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * 自定义跑马灯
 * <p>
 * Created by wongxd on 2018/11/5.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public class MyText extends AppCompatTextView implements OnClickListener {

    public final static String TAG = MyText.class.getSimpleName();

    private float textLength = 0f;// 文本长度
    private float viewWidth = 0f;
    private float step = 0f;// 文字的横坐标
    private float y = 0f;// 文字的纵坐标
    private float temp_view_plus_text_length = 0.0f;// 用于计算的临时变量
    private float temp_view_plus_two_text_length = 0.0f;// 用于计算的临时变量
    public boolean isStarting = false;// 是否开始滚动
    private Paint paint = null;// 绘图样式
    private String text = "";// 文本内容
    private int currentScrollX;// 当前滚动的位置
    private boolean isStop = false;
    private int textWidth;

    public MyText(Context context) {
        this(context, null);


    }

    public MyText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MyText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        try {
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        setOnClickListener(this);
    }

    /**
     * 文本初始化，每次更改文本内容或者文本效果等之后都需要重新初始化一下
     */
    public void init(WindowManager windowManager) {
        try {

            paint = getPaint();
            paint.setColor(getTextColors().getDefaultColor());
            text = getText().toString();
            textLength = paint.measureText(text);
            viewWidth = getWidth();
            if (viewWidth == 0) {
                if (windowManager != null) {
                    Display display = windowManager.getDefaultDisplay();
                    viewWidth = display.getWidth();
                }
            }
            step = textLength;
            temp_view_plus_text_length = viewWidth + textLength;
            temp_view_plus_two_text_length = viewWidth + textLength * 2;
            y = getTextSize() + getPaddingTop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 开始滚动
     */
    public void startScroll() {
        try {
            isStarting = true;
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 停止滚动
     */
    public void stopScroll() {
        try {
            isStarting = false;
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDraw(Canvas canvas) {

        try {
            canvas.drawText(text, temp_view_plus_text_length - step, y, paint);
            if (!isStarting) {
                return;
            }
            step += 2;// 速度
            if (step > temp_view_plus_two_text_length)
                step = textLength;
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 控制
    @Override
    public void onClick(View v) {
        try {
            if (isStarting)
                stopScroll();
            else
                startScroll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
