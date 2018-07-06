package com.wx.sportmap.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wx.sportmap.R;
import com.wx.sportmap.utils.StringIdUtils;


/**
 * 电量不足提示框
 * Created by admin on 2017/6/1.
 */

public class NormalTipDialog extends Dialog implements View.OnClickListener{


    private TextView tv_title,tv_tipContent;
    private String mTitle;
    private String mContent;
    private LinearLayout ll_bottom1;
    private LinearLayout ll_bottom2;
    private TextView tv_negative;
    private TextView tv_positive;

    public NormalTipDialog(@NonNull Context context) {
        super(context, R.style.selectorDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_normal_tip);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }

    private void initView() {
      TextView tv_iknow = (TextView) findViewById(R.id.tv_iknow);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_tipContent = (TextView) findViewById(R.id.tv_tipContent);
        tv_iknow.setOnClickListener(this);

        ll_bottom1 = (LinearLayout) findViewById(R.id.ll_bottom1);
        ll_bottom2 = (LinearLayout) findViewById(R.id.ll_bottom2);
        tv_negative = (TextView) findViewById(R.id.tv_negative);
        tv_positive = (TextView) findViewById(R.id.tv_positive);
        tv_negative.setOnClickListener(this);
        tv_positive.setOnClickListener(this);
        ll_bottom1.setVisibility(View.VISIBLE);
        ll_bottom2.setVisibility(View.GONE);

    }
    private int mType = 1;
    public NormalTipDialog setType(int flag){
        this.mType = flag;
        return this;
    }

    public NormalTipDialog title(String title){
        mTitle = title;
        return this;
    }
    public NormalTipDialog content(String content){
        mContent = content;
        return this;
    }

    public NormalTipDialog title(int  resId){
        mTitle =   StringIdUtils.getInstance().getString(resId);
        return this;
    }
    public NormalTipDialog content(int resId){
        mContent =   StringIdUtils.getInstance().getString(resId);
        return this;
    }
    @Override
    public void show() {
        super.show();
        if(tv_title != null && !TextUtils.isEmpty(mTitle))
            tv_title.setText(mTitle);
        if(tv_tipContent != null && !TextUtils.isEmpty(mContent))
            tv_tipContent.setText(mContent);
        if(mType == 1){
            ll_bottom1.setVisibility(View.VISIBLE);
            ll_bottom2.setVisibility(View.GONE);
        }else if(mType == 2)  {
            ll_bottom1.setVisibility(View.GONE);
            ll_bottom2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_iknow :
                if(mIKnowClickListener != null)
                    mIKnowClickListener.onClick();
                dismiss();
                break;
            case R.id.tv_positive :
                if(mSingleButtonOnClick != null)
                    mSingleButtonOnClick.positive();
                dismiss();
                break;
            case R.id.tv_negative :
                if(mSingleButtonOnClick != null)
                    mSingleButtonOnClick.negative();
                dismiss();
                break;
        }
    }

    private OnIKnowClickListener mIKnowClickListener;
    public interface OnIKnowClickListener{
        void onClick();
    }
    public NormalTipDialog setIknowClickListener(OnIKnowClickListener listener){
        this.mIKnowClickListener = listener;
        return this;
    }


    private SingleButtonOnClick mSingleButtonOnClick;
    public interface SingleButtonOnClick{
        void positive();//确认
        void negative();//取消
    }
    public NormalTipDialog setSingleButtonOnClick(SingleButtonOnClick callback){
        this.mSingleButtonOnClick = callback;
        return this;
    }

}
