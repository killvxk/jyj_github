package com.example.myimage.app;

import java.util.ArrayList;
import java.util.Date;

import com.example.myimage.ctrl.ListByTime;
import com.example.myimage.ctrl.AdapterTime.EditModeListener;
import com.example.myimage.data.ImageMgr;
import com.example.myimage.data.ImageMgr.ImageInfo;
import com.example.myimage.data.ImageMgr.ImageMgrListener;

import com.example.myimage.R;





import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewLike extends RelativeLayout implements ImageMgrListener {
	private ListByTime mList;
	public ViewLike(Context context, android.util.AttributeSet attrs) {
		super(context, attrs);

		// TODO Auto-generated constructor stub

	}

	public void init() {
		mList = (ListByTime)findViewById(R.id.listbytime);
		mList.Init();
		mList.setEnablePullRefresh(false);
		ImageMgr.GetInstance().addListener(this);
		mList.setEmptyText(R.string.no_like_tip);
		rereshData();
	}

	@Override
	public void onImageMgrNotify(int type, Object path) {
		// TODO Auto-generated method stub
		if ( type == ImageMgr.delete_end
				|| type == ImageMgr.like
				|| type==ImageMgr.refresh_end)
			rereshData();


	}

	private void rereshData() {
		ArrayList<ImageInfo> array = ImageMgr.GetInstance().GetLikeArray();

		mList.setArray(array);

	}
	public void setEditModeListener(EditModeListener listener) {
		// TODO Auto-generated method stub
		mList.setEditModeListener(listener);
	}

	public boolean haveData() {
		// TODO Auto-generated method stub
		return mList.haveData();
	}

	public void setEditMode(boolean b) {
		// TODO Auto-generated method stub
		mList.setEditMode(b);
	}
	public boolean getEditMode() {
		// TODO Auto-generated method stub
		return mList.GetEditMode();
	}
}