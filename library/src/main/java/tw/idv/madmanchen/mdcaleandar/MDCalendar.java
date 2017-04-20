package tw.idv.madmanchen.mdcaleandar;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import tw.idv.madmanchen.mdcaleandar.databinding.ViewMdcalendarBinding;
import tw.idv.madmanchen.mdcaleandar.databinding.ViewMdcalendarCellBinding;

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
    private ViewMdcalendarBinding mBinding;
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
            int titleBarColor = typedArray.getColor(R.styleable.MDCalendar_titleBarColor, Color.BLACK);
            mBinding.titleBarLl.setBackgroundColor(titleBarColor);

            int calendarBg = typedArray.getResourceId(R.styleable.MDCalendar_calendarBg, R.color.white);
            mBinding.calendarLl.setBackgroundResource(calendarBg);

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
        mBinding = DataBindingUtil.bind(view);
        mBinding.dateTitleTv.setText(mCalendarTitleFormat.format(mNowCalendar.getTime()));
        mBinding.dateVp.setAdapter(mCalendarPagerAdapter);
        mBinding.dateVp.addOnPageChangeListener(mPageChangeListener);
        mBinding.dateVp.setCurrentItem(mPageLimit / 2);
        mBinding.todayIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.dateVp.setCurrentItem(mPageLimit / 2, true);
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
            mBinding.dateTitleTv.setText(mCalendarTitleFormat.format(calendar.getTime()));
            System.gc();
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
            calendar_gv.setAdapter(new CalendarAdapter(container, position));
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
                        System.gc();
                    }
                    if (mOnDateClickListener != null) {
                        mOnDateClickListener.onDateClick(view, calendar);
                    }
                }
            });

            calendar_gv.post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams params = mBinding.dateVp.getLayoutParams();
                    params.height = calendar_gv.getMeasuredHeight();
                    mBinding.dateVp.setLayoutParams(params);
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
        ViewGroup mContainer;
        Calendar mCalendar;
        int mDayOfWeek;

        CalendarAdapter(ViewGroup container, int position) {
            mContainer = container;
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
            ViewMdcalendarCellBinding binding = DataBindingUtil.bind(convertView);
            final Calendar fCalendar = Calendar.getInstance();
            fCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), 1);
            fCalendar.add(Calendar.DATE, position - mDayOfWeek + 1);

            if (mCalendar.get(Calendar.MONTH) == fCalendar.get(Calendar.MONTH)) {
                binding.dayTv.setTextColor(Color.BLACK);
                convertView.setBackgroundResource(inMonthDateResId);
            } else {
                binding.dayTv.setTextColor(Color.GRAY);
                convertView.setBackgroundResource(outMonthDateResId);
            }

            if (mDateFormat.format(fCalendar.getTime()).equals(selectDate)) {
                convertView.setBackgroundResource(selectedResId);
            } else if (mDateFormat.format(fCalendar.getTime()).equals(mDateFormat.format(mNowCalendar.getTime()))) {
                convertView.setBackgroundResource(todayResId);
            }

            binding.dayTv.setText(String.valueOf(fCalendar.get(Calendar.DATE)));
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
