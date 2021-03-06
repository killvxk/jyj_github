package com.nanshan.myimage.app;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.nanshan.myimage.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import android.net.Uri;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.imagelib.app.ViewAll;
import com.imagelib.app.ViewDir;
import com.imagelib.app.ViewLike;
import com.imagelib.ctrl.AdapterTime.EditModeListener;
import com.imagelib.data.Helper;
import com.imagelib.data.Helper.StatisticHelper;
import com.imagelib.data.ImageLoader2;
import com.imagelib.data.ImageMgr;
import com.imagelib.data.ImageMgr.ImageMgrListener;

public class ActivityMain extends Activity implements OnClickListener,
		EditModeListener, ImageMgrListener {

	private final static String TAG = "MainActivity";
	private View mBarBegin;

	private TextView mTitle;
	private Button mButtonOp;
	private Button mButtonCamera;
	private Button mButtonMenu;
	private Button mButtonAll;
	private Button mButtonDir;
	private Button mButtonLike;
	private ViewPager mViewPager;
	private View mProgress;
	private ViewAll mViewAll;
	private ViewDir mViewDir;
	private ViewLike mViewLike;
	private View mCurView;
	private HashMap<Integer, View> mViewMap = new HashMap<Integer, View>();
	
	private static final int view_all = 0;
	private static final int view_dir = 1;
	private static final int view_like = 2;

	public static final int req_capture = 0;

	private int mIndex = 0;

	private boolean mEditMode = false;

	static boolean mInited = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UmengUpdateAgent.update(this);
		setContentView(R.layout.activity_main);

		Helper.setStatisticHelper(new StatisticHelper(){

			@Override
			public void send(String id) {
				// TODO Auto-generated method stub
				//MobclickAgent.onEvent(ActivityMain.this, id);
				
				MobclickAgent.onEventValue(ActivityMain.this, id, new HashMap<String,String>(), 0);
			}
			
		});
		initCtrl();
		initData();

		if(mInited == false)
			mProgress.setVisibility(View.VISIBLE);
	}

	public void onResume() {
		super.onResume();

		MobclickAgent.onResume(this);

	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	private void initCtrl() {
		mTitle = (TextView) findViewById(R.id.top_title);
		mBarBegin = findViewById(R.id.bar_top);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);

		mButtonCamera = (Button) findViewById(R.id.button_camera);
		mButtonOp = (Button) findViewById(R.id.button_op);
		mButtonMenu = (Button) findViewById(R.id.button_menu);
		mProgress = findViewById(R.id.progress);

		mButtonCamera.setOnClickListener(this);
		mButtonOp.setOnClickListener(this);
		mButtonMenu.setOnClickListener(this);
		mButtonAll = (Button) findViewById(R.id.button_all);
		mButtonAll.setOnClickListener(this);
		mButtonDir = (Button) findViewById(R.id.button_dir);
		mButtonDir.setOnClickListener(this);
		mButtonLike = (Button) findViewById(R.id.button_like);
		mButtonLike.setOnClickListener(this);

		// mGridView = (GridView) findViewById(R.id.gridView1);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub

				mIndex = arg0;
				updateButton();

				if (mViewAll != null && mViewAll.getEditMode() == true) {
					mViewAll.setEditMode(false);
				}

				if (mViewLike != null && mViewLike.getEditMode() == true) {
					mViewLike.setEditMode(false);

				}

				if(mIndex == view_dir)
					mButtonOp.setVisibility(View.GONE);
				else
					mButtonOp.setVisibility(View.VISIBLE);
			}

		});

		mViewPager.setAdapter(new MyAdapter(this));
		updateButton();
	}

	private void initData() {
		ImageMgr.GetInstance().addListener(this);

	}

	private void updateButton() {
		mButtonAll.setTextColor(this.getResources().getColor(
				R.color.bottom_text));
		mButtonDir.setTextColor(this.getResources().getColor(
				R.color.bottom_text));
		mButtonLike.setTextColor(this.getResources().getColor(
				R.color.bottom_text));

		Drawable d = getResources().getDrawable(R.drawable.photo_normal);
		d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
		mButtonAll.setCompoundDrawables(null, d, null, null);

		d = getResources().getDrawable(R.drawable.folder_normal);
		d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
		mButtonDir.setCompoundDrawables(null, d, null, null);

		d = getResources().getDrawable(R.drawable.favorite_normal);
		d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
		mButtonLike.setCompoundDrawables(null, d, null, null);
		if (mIndex == view_all) {
			d = getResources().getDrawable(R.drawable.photo_hover);
			d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
			mButtonAll.setCompoundDrawables(null, d, null, null);
			// mButtonAll.setBackgroundResource(R.color.button_down);
			mButtonAll.setTextColor(Color.rgb(253, 183, 48));
			mTitle.setText(R.string.photo);
		}

		else if (mIndex == view_dir) {
			d = getResources().getDrawable(R.drawable.folder_hover);
			d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
			mButtonDir.setCompoundDrawables(null, d, null, null);
			// mButtonDir.setBackgroundResource(R.color.button_down);
			mButtonDir.setTextColor(Color.rgb(76, 199, 237));
			mTitle.setText(R.string.album);
		}

		else if (mIndex == view_like) {
			d = getResources().getDrawable(R.drawable.favorite_hover);
			d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
			mButtonLike.setCompoundDrawables(null, d, null, null);
			// mButtonLike.setBackgroundResource(R.color.button_down);
			mButtonLike.setTextColor(Color.rgb(243, 128, 139));
			mTitle.setText(R.string.favorite);
		}

	}

	private class MyAdapter extends PagerAdapter {

		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {

			View view = mViewMap.get(arg1);

			if (view == null) {
				if (arg1 == view_all) {

					// mViewAll = new ViewAll(this);

					mViewAll = (ViewAll) mInflater.inflate(R.layout.view_all,
							null);

					mViewAll.init();

					mViewAll.setEditModeListener(ActivityMain.this);

					view = mViewAll;

				} else if (arg1 == view_dir) {

					mViewDir = (ViewDir) mInflater.inflate(R.layout.view_dir,
							null);

					mViewDir.init();

					view = mViewDir;

				} else if (arg1 == view_like) {

					mViewLike = (ViewLike) mInflater.inflate(
							R.layout.view_like, null);

					mViewLike.init();

					mViewLike.setEditModeListener(ActivityMain.this);

					view = mViewLike;

				}
				mViewMap.put(arg1, view);
				((ViewPager) arg0).addView(view);
			}

			return view;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {

			// ((ViewPager) arg0).removeView((View) arg2);

		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}

	private void showSettingMenu() {

		LayoutInflater inflater = LayoutInflater.from(this);

		View view = inflater.inflate(R.layout.menu_setting, null);

		view.measure(0, 0);

		final PopupWindow menu = new PopupWindow(view,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);

		menu.setBackgroundDrawable(new BitmapDrawable());

		menu.setFocusable(true);
		menu.setOutsideTouchable(true);
		menu.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub

			}

		});

		view.findViewById(R.id.button_refresh).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						ImageMgr.GetInstance().refresh();

						menu.dismiss();

					}
				});

		menu.showAsDropDown(mButtonMenu, 0, -Helper.dp2px(this, 2) - 6);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.button_camera) {
			
			Helper.statistic("tab_camera");
			String sdStatus = Environment.getExternalStorageState();
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { //
				Log.d("MainActivity",
						"SD card is not avaiable/writeable right now.");
				return;
			}
			try {
				File dir = new File(Environment.getExternalStorageDirectory()
						+ "/DCIM/Camera");
				if (!dir.exists())
					dir.mkdirs();

				String name = DateFormat.format("yyyyMMdd_hhmmss",
						Calendar.getInstance(Locale.CHINA))
						+ ".jpg";

				File f = new File(dir, name);
				Intent intent0 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent0.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				intent0.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

				Intent intent = Intent.createChooser(intent0, null);

				mCameraPicPath = f.getAbsolutePath();

				startActivityForResult(intent, req_capture);

			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}

		} else if (id == R.id.button_op) {
			Helper.statistic("btn_edit");
			if (mIndex == view_all) {
				if (mViewAll.haveData())
					mViewAll.setEditMode(true);
			} else if (mIndex == view_like) {
				if (mViewLike.haveData())
					mViewLike.setEditMode(true);
			}

		} else if (id == R.id.button_menu) {

			{
				showSettingMenu();
			}
		} else if (id == R.id.button_all) {
			mViewPager.setCurrentItem(view_all);
			
			Helper.statistic("tab_photo");
		} else if (id == R.id.button_dir) {
			mViewPager.setCurrentItem(view_dir);
			Helper.statistic("tab_album");
		} else if (id == R.id.button_like) {
			mViewPager.setCurrentItem(view_like);
			//MobclickAgent.onEventValue(this, "tab_collect", new HashMap<String,String>(), 0);
			Helper.statistic("tab_collect");
		}
	}

	String mCameraPicPath;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == req_capture) {
			if (resultCode == Activity.RESULT_OK) {
				try {
					File f = new File(mCameraPicPath);
					if (f != null && f.exists() && f.isFile()) {
						ImageMgr.GetInstance().addImage(mCameraPicPath);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK/* && event.getRepeatCount() == 0 */) {
			if (mIndex == view_all) {
				if (mViewAll.getEditMode() == true) {
					mViewAll.setEditMode(false);
					return true;
				}
			} else if (mIndex == view_like) {
				if (mViewLike.getEditMode() == true) {
					mViewLike.setEditMode(false);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		// GlobalManager.getInstance().uploadManager().stopNow();
		// GlobalManager.getInstance().destroyNow();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		ImageLoader2.GetInstance().clearCache();

		super.onDestroy();
	}

	@Override
	public void onSelCountChange(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEditModeBegin() {
		// TODO Auto-generated method stub
		mBarBegin.setVisibility(View.GONE);
	}

	@Override
	public void onEditModeFinish() {
		// TODO Auto-generated method stub
		mBarBegin.setVisibility(View.VISIBLE);
		if (mIndex == view_all)
			mViewAll.setEditMode(false);
		else if (mIndex == view_like)
			mViewLike.setEditMode(false);
	}

	@Override
	public void onImageMgrNotify(int type, Object path) {
		// TODO Auto-generated method stub
		if (type == ImageMgr.refresh) {
			if(mProgress != null)
			mProgress.setVisibility(View.GONE);
			mInited = true;
		} else if (type == ImageMgr.delete_begin) {

		} else if (type == ImageMgr.delete_end) {

		}
	}

}
