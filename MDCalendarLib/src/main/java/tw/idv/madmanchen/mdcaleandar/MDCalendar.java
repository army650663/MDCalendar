package tw.idv.madmanchen.mdcaleandar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/4/18      chenshaowei         V1.0.0          Create
 * Why & What is modified:
 */

public class MDCalendar extends LinearLayout {
    private Context mContext;
    private LinearLayout mCalendar_ll;
    private LinearLayout mTitleBar_ll;
    private TextView mDateTitle_tv;
    private ViewPager mMonth_vp;
    private ImageView mGoToday_iv;

    // Attr
    private int todayResId;
    private int selectedResId;
    private int inMonthDateResId;
    private int outMonthDateResId;

    private Calendar mNowCalendar;
    private CalendarPagerAdapter mCalendarPagerAdapter;
    private SimpleDateFormat mCalendarTitleFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    private int mPageLimit = 100;
    private String selectDate = "";

    public interface OnDateClickListener {
        void onDateClick(View view, Calendar calendar);
    }

    private OnDateClickListener mOnDateClickListener;
    private HashMap<String, Object> mDateDataMap = new HashMap<>();


    public MDCalendar(Context context) {
        super(context);
        initView(context);
    }

    public MDCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MDCalendar);
        try {
            int titleBarColor = typedArray.getColor(R.styleable.MDCalendar_titleBarColor, ContextCompat.getColor(context, R.color.darkBlue));
            mTitleBar_ll.setBackgroundColor(titleBarColor);

            int calendarBg = typedArray.getResourceId(R.styleable.MDCalendar_calendarBg, R.color.white);
            mCalendar_ll.setBackgroundResource(calendarBg);

            todayResId = typedArray.getResourceId(R.styleable.MDCalendar_todayCellBg, R.drawable.sty_today);
            selectedResId = typedArray.getResourceId(R.styleable.MDCalendar_selectedDateBg, R.drawable.sty_date_selected);
            inMonthDateResId = typedArray.getResourceId(R.styleable.MDCalendar_inMonthDateBg, R.drawable.sty_in_month);
            outMonthDateResId = typedArray.getResourceId(R.styleable.MDCalendar_outMonthDateBg, R.drawable.sty_out_month);
        } finally {
            typedArray.recycle();
        }
    }

    private void initView(final Context context) {
        mContext = context;
        mNowCalendar = Calendar.getInstance();
        mCalendarPagerAdapter = new CalendarPagerAdapter();
        View view = inflate(context, R.layout.view_mdcalendar, null);
        addView(view);
        mCalendar_ll = (LinearLayout) view.findViewById(R.id.calendar_ll);
        mTitleBar_ll = (LinearLayout) view.findViewById(R.id.titleBar_ll);
        mDateTitle_tv = (TextView) view.findViewById(R.id.dateTitle_tv);
        mMonth_vp = (ViewPager) view.findViewById(R.id.month_vp);
        mGoToday_iv = (ImageView) view.findViewById(R.id.goToday_iv);

        mDateTitle_tv.setText(mCalendarTitleFormat.format(mNowCalendar.getTime()));
        mMonth_vp.setAdapter(mCalendarPagerAdapter);
        mMonth_vp.addOnPageChangeListener(mPageChangeListener);
        mMonth_vp.setCurrentItem(mPageLimit / 2);
        mGoToday_iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMonth_vp.setCurrentItem(mPageLimit / 2, true);
            }
        });
    }

    public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
        mOnDateClickListener = onDateClickListener;
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Calendar calendar = getCalendarFromPosition(position);
            mDateTitle_tv.setText(mCalendarTitleFormat.format(calendar.getTime()));
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private class CalendarPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPageLimit;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final WCHGridView calendar_gv = new WCHGridView(mContext);
            calendar_gv.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            calendar_gv.setNumColumns(7);
            calendar_gv.setAdapter(new CalendarAdapter(position));
            calendar_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    Calendar calendar = (Calendar) view.getTag();
                    if (!selectDate.equals(mDateFormat.format(calendar.getTime()))) {
                        selectDate = mDateFormat.format(calendar.getTime());
                        for (int i = 0; i < container.getChildCount(); i++) {
                            GridView gv = (GridView) container.getChildAt(i);
                            CalendarAdapter adapter = (CalendarAdapter) gv.getAdapter();
                            adapter.notifyDataSetChanged();
                        }
                    }
                    if (mOnDateClickListener != null) {
                        mOnDateClickListener.onDateClick(view, calendar);
                    }
                }
            });

            calendar_gv.post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams params = mMonth_vp.getLayoutParams();
                    params.height = calendar_gv.getMeasuredHeight();
                    mMonth_vp.setLayoutParams(params);
                }
            });

            container.post(new Runnable() {
                @Override
                public void run() {
                    container.addView(calendar_gv);
                }
            });
            return calendar_gv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class CalendarAdapter extends BaseAdapter {
        Calendar mCalendar;
        int mDayOfWeek;

        CalendarAdapter(int position) {
            mCalendar = getCalendarFromPosition(position);
            mDayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        }

        @Override
        public int getCount() {
            return 42;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.view_mdcalendar_cell, null, false);
            }
            TextView day_tv = (TextView) convertView.findViewById(R.id.day_tv);

            final Calendar fCalendar = Calendar.getInstance();
            fCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), 1);
            fCalendar.add(Calendar.DATE, position - mDayOfWeek + 1);

            if (mCalendar.get(Calendar.MONTH) == fCalendar.get(Calendar.MONTH)) {
                day_tv.setTextColor(Color.BLACK);
                convertView.setBackgroundResource(inMonthDateResId);
            } else {
                day_tv.setTextColor(Color.GRAY);
                convertView.setBackgroundResource(outMonthDateResId);
            }

            if (mDateFormat.format(fCalendar.getTime()).equals(selectDate)) {
                convertView.setBackgroundResource(selectedResId);
            } else if (mDateFormat.format(fCalendar.getTime()).equals(mDateFormat.format(mNowCalendar.getTime()))) {
                convertView.setBackgroundResource(todayResId);
            }

            day_tv.setText(String.valueOf(fCalendar.get(Calendar.DATE)));
            convertView.setTag(fCalendar);
            return convertView;
        }
    }

    private Calendar getCalendarFromPosition(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, position - mPageLimit / 2);
        calendar.set(Calendar.DATE, 1);
        return calendar;
    }


}
