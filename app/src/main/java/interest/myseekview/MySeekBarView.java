package interest.myseekview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Created by jet 2017/3/7.
 */
public class MySeekBarView extends View {
    //画圆柱条的矩形
    private RectF line = new RectF();
    //圆柱背景的画笔
    private Paint bgPaint=new Paint();
    //文本画笔
    private Paint textPaint=new Paint();
    //圆柱条的半径
    private float mConerRadius;
    //拖动按钮
    private Bitmap mBitmap;

    private int mSeekBarRadius;
    //View的高度
    private int mHeight;
    //view的宽度
    private int mWidth;
    //矩形的2个点坐标
    private int mLineLeft;
    private int mLineTop;
    private int mLineRight;
    private int mLineBottom;
    //圆柱宽度
    private int lineWidth;
    //滑动的百分比
    private float mPercent;
    //取小数点后1位
    private DecimalFormat mDf;
    //取小数点后2位
    private DecimalFormat mDff;
    private Context mContext;
    //判断类型,金额还是期限
    private boolean mIsTime;
    //滚动条的背景
    private int mSeekBarBg;
    //滚动条选择的背景
    private int mSeekBarSelectBg;
    private boolean isScroll;

    //最小期限
    private   int MIN_TERM=3;
    //相对与滚动条最大期限
    private   int MAX_TERM=33;
    //最大总期限
    private   int TOTAL_TERM=MIN_TERM+MAX_TERM;
    //最小额度
    private   int MIN_MONEY=30000;
    //相对与滚动条最大额度
    private   int MAX_MONEY=120000;
    //最大总额度
    private   int TOTAL_MONEY=MIN_MONEY+MAX_MONEY;
    //额度单位值
    private   int UNIT_MONEY=10000;

    public MySeekBarView(Context context) {
        super(context);
        initAttrs(context,null);
        init(context);
    }
    public MySeekBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context,attrs);
        init(context);
    }

    public MySeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context,attrs);
        init(context);
    }
   public void  initAttrs(Context context,AttributeSet attrs){
       if(attrs!=null){
           TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.mySeekBarView);
           mIsTime = a.getBoolean(R.styleable.mySeekBarView_isTime, false);
           isScroll =a.getBoolean(R.styleable.mySeekBarView_isScroll, true);
           mSeekBarBg = a.getColor(R.styleable.mySeekBarView_default_bg, getResources().getColor(R.color.seekBar_bg));
           mSeekBarSelectBg = a.getColor(R.styleable.mySeekBarView_select_bg, getResources().getColor(R.color.seekBar_selector_bg));
       }else {
           mIsTime=false;
           isScroll=true;
           mSeekBarBg=getResources().getColor(R.color.seekBar_bg);
           mSeekBarSelectBg= getResources().getColor(R.color.seekBar_selector_bg);
       }
   }

    private void init(Context context) {
        int TEXT_SIZE = Math.round(20 * getTextSize(context));
        mDf = new DecimalFormat("0.#");
        mDff= new DecimalFormat("#.00");
        bgPaint.setColor(mSeekBarBg);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setAntiAlias(true);
        textPaint.setColor(mSeekBarSelectBg);
        // 从资源文件中生成位图bitmap
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.hua_btn2);
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        //取该view高度的4分之一做seekBar
        mSeekBarRadius = h / 4;
        //确定矩形的左上点
        mLineLeft = mSeekBarRadius;
        mLineTop = mSeekBarRadius - mSeekBarRadius /3;
        mLineRight = w- mSeekBarRadius;
        mLineBottom = mSeekBarRadius + mSeekBarRadius /3;

        line.set(mLineLeft, mLineTop, mLineRight, mLineBottom);
        //设置圆角的半径
        mConerRadius = (mLineBottom - mLineTop)*0.5f;
        //圆柱体长度
        lineWidth = mLineRight - mLineLeft;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSize * 2 > widthSize) {
            setMeasuredDimension(widthSize, widthSize / 2);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画默认seekBar背景
        canvas.drawRoundRect(line,mConerRadius,mConerRadius,bgPaint);
        //画已激活的seekBar背景,左上点不变
        RectF selectRect=new RectF(mLineLeft, mLineTop, (int) ((mPercent*lineWidth))+mLineLeft,mLineBottom);
        canvas.drawRoundRect(selectRect,mConerRadius,mConerRadius,textPaint);
        //view层大于按钮图的时候
        if(mHeight>mBitmap.getHeight()){
            canvas.drawBitmap(mBitmap,mLineLeft+(mPercent*lineWidth)-(mPercent*mBitmap.getWidth()),mHeight/4-(mBitmap.getHeight()/2),new Paint());
        }else {
//            canvas.drawBitmap(mBitmap,(mSeekBarRadius/4)+(mPercent*lineWidth),-((mBitmap.getHeight()/2)-mSeekBarRadius)+3,new Paint());
        }
        //画字体 文本X的位置,滑动的长度+mLineLeft
       float textWidth= mPercent*lineWidth+mLineLeft;
        String text="";
        if(mIsTime){
            text=(int)timePercent+"期";
        }else {
            text= loanPercent+"";
        }
        //计算文本高度,保证在View最底部
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
       float textHeight= mHeight-rect.height();
        //文本的x位置随百分值减少
        canvas.drawText(text,textWidth-(rect.width()*mPercent),textHeight,textPaint);
        oldtimePercent=timePercent;

    }
    //还款模式下的期限
    private double timePercent=MIN_TERM;
    //上一次滑动的期限
    private double oldtimePercent;
    //申请金额
    private int loanPercent=MIN_MONEY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) {

                    getParent().requestDisallowInterceptTouchEvent(true);

                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if(!isScroll)return true;
                float x = event.getX();
                if (x <= mLineLeft) {
                    mPercent = 0;
                    timePercent=MIN_TERM;
                } else if (x >= mLineRight){
                    mPercent = 1;
                    timePercent=TOTAL_TERM;
                } else {
                    mPercent = (x - (mLineLeft) )* 1f / (lineWidth);
                    //最低为3期+line长度(33期)=36
                    timePercent= (int) (mPercent*MAX_TERM)+MIN_TERM;
                    //line长度分12万+最低3万=15万
                    double loanP=Math.round(mPercent*MAX_MONEY);
                        loanPercent=(int)(Math.round(loanP/UNIT_MONEY)*UNIT_MONEY) +MIN_MONEY;


                    //36期有12份,判断是否是整除3,是?正确期的格式:不正确
                    //不正确还是显示上一个期限
                    //3为间隔数
                    if(Math.abs(timePercent-oldtimePercent)>=3){
                        if(timePercent%3!=0){
                            if((timePercent+1)%3==0){
                                timePercent=timePercent+1;
                            }else {
                                timePercent=(timePercent+2);
                            }
                        }

                    }else {
                        if(timePercent%3!=0){
                            timePercent=oldtimePercent;
                        }
                    }

                }
                // SeekBar按钮根据当前手指在拖动条上的滑动而滑动
//                seekbar.slide(percent);

                    invalidate();

                if(mCallBack!=null){
                    if(mIsTime){
                        mCallBack.OnSelect((int) timePercent);
                    }else {
                        mCallBack.OnSelect(loanPercent);
                    }

                }

                return true;
            case MotionEvent.ACTION_UP:

                break;
        }
        return super.onTouchEvent(event);
    }
    //获取不同分辨力的适配大小
    public float getTextSize(Context context){
        JDisplayMetrics metrics= AppUtil.getDisplayMetrics(context);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        float ratioWidth = (float)screenWidth / 480;
        float ratioHeight = (float)screenHeight / 800;
        float RATIO = Math.max(ratioWidth, ratioHeight);
        return RATIO;
    }
    public OnSelectCallBack mCallBack;

    public void setCallBack(OnSelectCallBack callBack) {
        mCallBack = callBack;
    }

    public interface  OnSelectCallBack{
        public void OnSelect(int index);
    }

    public void setInitTermValues(int termValues){

        timePercent=termValues;
        oldtimePercent=timePercent;
        mPercent= (float) ((timePercent-MIN_TERM)/MAX_TERM);
        this.invalidate();
    }
    public void setInitMoneyValues(int moneyValues){
        loanPercent=moneyValues;
        LogUtil.debugE("mydaso",loanPercent+"");
        if(loanPercent<MIN_MONEY){
            loanPercent=MIN_MONEY;
        }
        mPercent=(float)(loanPercent-MIN_MONEY)/(float)MAX_MONEY;
        LogUtil.debugE("myo",loanPercent-MIN_MONEY/(float)MAX_MONEY+"");
        this.invalidate();
    }

    public void setMAX_TERM(int MAX_TERM) {
        this.MAX_TERM = MAX_TERM;
    }

    public void setMAX_MONEY(int MAX_MONEY) {
        this.MAX_MONEY = MAX_MONEY;
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }
}
