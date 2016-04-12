package com.feng.flowlayout;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by feng on 2016/4/9.
 */
public class FlowLayout extends ViewGroup {

    //全部行数的集合
    private ArrayList<ArrayList<View>> allLine = new ArrayList<>();
    private int horizotalSpacing = 6;
    private int verticalSpacing = 6;

    public FlowLayout(Context context) {
        super(context);
    }

    /**
     * 测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        allLine.clear();    // 因为onMeasure方法会执行多次，所以每次测量前先把之前的数据清空
        int containerMeasuredWidth = MeasureSpec.getSize(widthMeasureSpec);

        ArrayList<View> oneLine = null;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);    // 获取子View
            // 把测量规格传给子View，让子View完成测量
            child.measure(0, 0);
            // 如果是第0个需要创建新行，或者当前View的宽度大于一行中剩余的可用宽度也需要创建新行
            if (i == 0 || child.getMeasuredWidth() > getUsableWidth(containerMeasuredWidth, oneLine)) {
                oneLine = new ArrayList<View>();
                allLine.add(oneLine);
            }
            oneLine.add(child);
        }

        //获取所有行的高
        int containerMeasuredHeight = getAllLinesHeight() + getPaddingTop() + getPaddingBottom();

        MeasureSpecUtil.printMeasureSpec(widthMeasureSpec, heightMeasureSpec);
        // 设置FlowLayout的宽和高，宽就用父容器传的宽，高用子View的高
        setMeasuredDimension(containerMeasuredWidth, containerMeasuredHeight);
    }

    /**
     * 获取所有行的高
     *
     * @return
     */
    private int getAllLinesHeight() {
        if (allLine.isEmpty()) {
            return 0;
        } else {
            //一行的高度乘以一共多少行
            int allSpacing = verticalSpacing * (allLine.size() - 1);
            return getChildAt(0).getMeasuredHeight() * allLine.size() + allSpacing;
        }
    }

    /**
     * 获取可用的宽度
     *
     * @param containerMeasuredWidth
     * @param oneLine
     * @return
     */
    private int getUsableWidth(int containerMeasuredWidth, ArrayList<View> oneLine) {
        return containerMeasuredWidth - getOneLineWidth(oneLine) - getPaddingLeft() - getPaddingRight();
    }

    /**
     * 获取一行的宽度
     *
     * @param oneLine
     * @return
     */
    private int getOneLineWidth(ArrayList<View> oneLine) {
        int oneLineWidth = 0;
        for (View view : oneLine) {
            //之前已经测量过了,所以可以获取到测量后的宽度
            oneLineWidth += view.getMeasuredWidth();
        }
        int allSpacing = horizotalSpacing * (oneLine.size() - 1);
        return oneLineWidth + allSpacing;
    }


    /**
     * 排版
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //遍历所有的行
        int tempBottom = 0;    // 用于临时保存当前行底部的坐标
        for (int i = 0; i < allLine.size(); i++) {

            //获取一行
            ArrayList<View> oneLine = allLine.get(i);
            //遍历一行中的所有列
            int tempRight = 0; // 用于临时保存当前子View的Right坐标

            //把一行中剩余的空间平均分给一行中的所有view  这是onMeasure方法已经执行完毕 所以可以获取FlowLayout测量宽度
            int averageUsableWidth = getUsableWidth(getMeasuredWidth(), oneLine) / oneLine.size();
            for (int j = 0; j < oneLine.size(); j++) {
                // 获取一行中的子View
                View child = oneLine.get(j);


                // 获取子View的测量宽和测量高
                int childMeasuredWidth = child.getMeasuredWidth();
                int childMeasuredHeight = child.getMeasuredHeight();

                // 子View的left坐标为上一个子View的right坐标
                int childLeft = j == 0 ? getPaddingLeft() : tempRight + horizotalSpacing;

                // 子View的top坐标为上一个子View的bottom坐标
                int childTop = i == 0 ? getPaddingTop() : tempBottom + verticalSpacing;

                int childRight = (j == oneLine.size() - 1) ? getMeasuredWidth() - getPaddingRight() : childLeft + childMeasuredWidth + averageUsableWidth;
                int childBottom = childTop + childMeasuredHeight;
                child.layout(childLeft, childTop, childRight, childBottom);

                int widthMeasureSpec = MeasureSpec.makeMeasureSpec(childRight - childLeft, MeasureSpec.EXACTLY);
                int heightMeasureSpec = MeasureSpec.makeMeasureSpec(childBottom - childTop, MeasureSpec.EXACTLY);
                child.measure(widthMeasureSpec, heightMeasureSpec);

                // 保存当前列的right坐标，用于下次使用
                tempRight = childRight;
            }
            // 保存当行行的bottom坐标，用于下次使用
            tempBottom = oneLine.get(0).getBottom();

        }
    }
}
