package com.bdsdk.update;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.CPUpdateDownloadCallback;
import com.baidu.integrationsdk.lib.R;

public class BaiDuAutoUpdatePopupwindow {
	private Context con;
	private PopupWindow pop;
	private LinearLayout ll_bt;
	private LinearLayout ll_pb;
	private ProgressBar pb_load;
	private TextView tv_progress;
	
	public BaiDuAutoUpdatePopupwindow(Context con) {
		super();
		this.con = con;
	}
	
	public void startCheck(){
		BDAutoUpdateSDK.cpUpdateCheck(con, new MyCPCheckUpdateCallback());
	}
	private class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

		@Override
		public void onCheckUpdateCallback(AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
			showPop(info, infoForInstall);
		}

	}

	public void showPop(final AppUpdateInfo info, final AppUpdateInfoForInstall infoForInstall){
		if(infoForInstall != null && !TextUtils.isEmpty(infoForInstall.getInstallPath())) {
			System.out.println("---infoForInstall---");
//			BDAutoUpdateSDK.cpUpdateInstall(con, infoForInstall.getInstallPath());
		}else if(info != null) {
			System.out.println("---info---");
//			BDAutoUpdateSDK.cpUpdateDownload(con, info, new UpdateDownloadCallback());
		}else {
			return;
		}
		String vsersion = con.getString(R.string.bdp_update_info_version)+info.getAppVersionName();
		long size = info.getAppSize();
		System.out.println("size:"+size);
		String newSize =  con.getString(R.string.bdp_update_info_apksize)+new java.text.DecimalFormat("0.00").format((float)size/1000000)+"M";
		String str =  con.getString(R.string.bdp_update_info_log)+"\n"+info.getAppChangeLog();
		String log = str.replaceAll("<br>", "\n");
		
		View v = LayoutInflater.from(con).inflate(R.layout.baidupdate_layout, null);
		TextView tv_cancel = (TextView) v.findViewById(R.id.tv_cancel);
		TextView tv_info = (TextView) v.findViewById(R.id.tv_info);
		tv_progress = (TextView) v.findViewById(R.id.tv_progress);
		tv_progress.setText("0%");
		pb_load = (ProgressBar) v.findViewById(R.id.pb_load);
		pb_load.setMax(100);
		ll_pb = (LinearLayout) v.findViewById(R.id.ll_pb);
		ll_bt = (LinearLayout) v.findViewById(R.id.ll_bt);
		tv_info.setText(vsersion+"\n"+newSize+"\n"+log);
		
		tv_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				pop.dismiss();
			}
		});
		TextView tv_ensure = (TextView) v.findViewById(R.id.tv_ensure);
		tv_ensure.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(infoForInstall != null && !TextUtils.isEmpty(infoForInstall.getInstallPath())) {
					BDAutoUpdateSDK.cpUpdateInstall(con, infoForInstall.getInstallPath());
				}else if(info != null) {
					ll_bt.setVisibility(View.GONE);
					ll_pb.setVisibility(View.VISIBLE);
					BDAutoUpdateSDK.cpUpdateDownload(con, info, new UpdateDownloadCallback());					
				}
				
			}
		});
		pop = new PopupWindow(v);
		pop.setWidth(-1);
		pop.setHeight(-1);
		
		Activity ac = (Activity) con;
		pop.showAsDropDown(ac.getWindow().getDecorView(), 0, -ac.getWindowManager().getDefaultDisplay().getHeight());
		
	}
	
	private class UpdateDownloadCallback implements CPUpdateDownloadCallback {

		@Override
		public void onDownloadComplete(String apkPath) {
//			txt_log.setText(txt_log.getText() + "\n onDownloadComplete: " + apkPath);
			BDAutoUpdateSDK.cpUpdateInstall(con, apkPath);
			pop.dismiss();
		}

		@Override
		public void onStart() {
//			txt_log.setText(txt_log.getText() + "\n Download onStart");
		}

		@Override
		public void onPercent(int percent, long rcvLen, long fileSize) {
			tv_progress.setText(percent+"%");
			pb_load.setProgress(percent);
//			txt_log.setText(txt_log.getText() + "\n Download onPercent: " + percent + "%");
		}

		@Override
		public void onFail(Throwable error, String content) {
//			txt_log.setText(txt_log.getText() + "\n Download onFail: " + content);
		}

		@Override
		public void onStop() {
//			txt_log.setText(txt_log.getText() + "\n Download onStop");
		}
		
	}
	

}
