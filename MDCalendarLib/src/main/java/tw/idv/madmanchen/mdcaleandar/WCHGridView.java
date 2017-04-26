package tw.idv.madmanchen.mdcaleandar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Author:      chenshaowei
 * Version      V1.0
 * Description:
 * Modification History:
 * Date         Author          version         Description
 * ---------------------------------------------------------------------
 * 2017/4/20      chenshaowei         V1.0.0          Create
 * Why & What is modified:
 */

public class WCHGridView extends GridView {

    public WCHGridView(Context context) {
        super(context);
    }

    public WCHGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
