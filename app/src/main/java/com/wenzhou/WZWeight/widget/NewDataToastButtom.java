package com.wenzhou.WZWeight.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wenzhou.WZWeight.R;

public class NewDataToastButtom extends Toast {

	private MediaPlayer mPlayer;
	private boolean isSound;

	public NewDataToastButtom(Context context) {
		this(context, false);
	}

	public NewDataToastButtom(Context context, boolean isSound) {
		super(context);

		this.isSound = isSound;

		mPlayer = MediaPlayer.create(context, R.raw.off);
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});

	}

	@Override
	public void show() {
		super.show();

		if (isSound) {
			mPlayer.start();
		}
	}


	public void setIsSound(boolean isSound) {
		this.isSound = isSound;
	}


	public static NewDataToastButtom makeText(Context context,
			CharSequence text, boolean isSound) {
		NewDataToastButtom result = new NewDataToastButtom(context, isSound);

		LayoutInflater inflate = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		DisplayMetrics dm = context.getResources().getDisplayMetrics();

		View v = inflate.inflate(R.layout.new_data_toast, null);
		v.setMinimumWidth(dm.widthPixels);

		TextView tv = (TextView) v.findViewById(R.id.new_data_toast_message);
		tv.setText(text);

		result.setView(v);
		result.setDuration(600);
		result.setGravity(Gravity.BOTTOM, 0, (int) (dm.density * 75));

		return result;
	}

}
