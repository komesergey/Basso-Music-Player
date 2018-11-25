package com.basso.basso.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.WearActivity;

public class ControlListAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final SharedPreferences pref;
    private float balance  = 1.0f;
    private int currentVolume = 0;
    private int maxVolume = 1;
    public ControlListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.pref = PreferenceManager.getDefaultSharedPreferences(mContext);

    }
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private ImageButton leftButton;
        private ImageButton centerButton;
        private ImageButton rightButton;
        private TextView description;
        private SeekBar controlSeekbar;
        private TextView controlName;
        private LinearLayout buttonLayout;
        private LinearLayout seekBarLayout;
        public ItemViewHolder(View itemView) {
            super(itemView);
            leftButton = (ImageButton)itemView.findViewById(R.id.leftButton);
            centerButton = (ImageButton)itemView.findViewById(R.id.centerButton);
            rightButton = (ImageButton)itemView.findViewById(R.id.rightButton);
            description = (TextView)itemView.findViewById(R.id.description);
            controlName = (TextView)itemView.findViewById(R.id.control_name);
            controlSeekbar = (SeekBar)itemView.findViewById(R.id.control_seekbar);
            buttonLayout = (LinearLayout)itemView.findViewById(R.id.buttons_layout);
            seekBarLayout  = (LinearLayout)itemView.findViewById(R.id.seekbar_layout);
        }

    }

    public void sendMessage(String path, String text){
        ((WearActivity)mContext).sendMessage(path,text);
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.control_list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        if(position == 0){
            itemHolder.description.setVisibility(View.GONE);
            ((WearActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    itemHolder.leftButton.setImageResource(R.drawable.btn_playback_previous);
                }
            });
            itemHolder.leftButton.setPadding(0,0,0,0);
            itemHolder.leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage("prevTrack", "");
                }
            });
            String playstate = pref.getString("playstate", "pause");
            if(playstate.equals("pause")){
                ((WearActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemHolder.centerButton.setImageResource(R.drawable.btn_playback_pause);
                        itemHolder.centerButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                    }
                });
            }else if(playstate.equals("play")){
                ((WearActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        itemHolder.centerButton.setImageResource(R.drawable.btn_playback_play);
                        itemHolder.centerButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                });
            }
            itemHolder.centerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage("playOrPause", "");
                }
            });
            ((WearActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    itemHolder.rightButton.setVisibility(View.VISIBLE);
                    itemHolder.rightButton.setImageResource(R.drawable.btn_playback_next);
                }
            });
            itemHolder.rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage("nextTrack","");
                }
            });
            itemHolder.seekBarLayout.setVisibility(View.GONE);
        }

        if(position == 1){
            String shufflestate = pref.getString("shufflestate", "none");
            String repeatstate = pref.getString("repeatstate","none");
            itemHolder.description.setVisibility(View.GONE);
            if(repeatstate.equals("none")) {
                itemHolder.leftButton.setImageResource(R.drawable.btn_playback_repeat);
            } else if(repeatstate.equals("all")){
                itemHolder.leftButton.setImageResource(R.drawable.btn_playback_repeat_all);
            } else if(repeatstate.equals("current")){
                itemHolder.leftButton.setImageResource(R.drawable.btn_playback_repeat_one);
            }
            itemHolder.leftButton.setPadding(0,0,(int)(10*mContext.getResources().getDisplayMetrics().density),0);
            itemHolder.leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage("setRepeatMode","");
                }
            });
            if(shufflestate.equals("none")) {
                itemHolder.centerButton.setImageResource(R.drawable.btn_playback_shuffle);
            }else if(shufflestate.equals("normal") || shufflestate.equals("auto")){
                itemHolder.centerButton.setImageResource(R.drawable.btn_playback_shuffle_all);
            }
            itemHolder.centerButton.setPadding((int) (10 * mContext.getResources().getDisplayMetrics().density), 0, 0, 0);
            itemHolder.centerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage("setShuffleMode","");
                }
            });
            itemHolder.rightButton.setVisibility(View.GONE);
            itemHolder.seekBarLayout.setVisibility(View.GONE);
        }

        if(position == 2){
            itemHolder.buttonLayout.setVisibility(View.GONE);
            itemHolder.seekBarLayout.setVisibility(View.VISIBLE);
            currentVolume =  Integer.parseInt(pref.getString("currentVolume","0"));
            maxVolume =  Integer.parseInt(pref.getString("maxVolume","1"));
            System.out.println("Current volume " + currentVolume + " max volume " + maxVolume);
            itemHolder.controlSeekbar.setMax(maxVolume);
            itemHolder.controlSeekbar.setProgress(currentVolume);
            itemHolder.controlSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentVolume = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    sendMessage("setVolume",currentVolume+"");
                }
            });
            itemHolder.controlName.setText(mContext.getString(R.string.volume));
        }

        if(position == 3){
            String balanceString = pref.getString("balance", "1.0f");
            balance = Float.parseFloat(balanceString);
            itemHolder.buttonLayout.setVisibility(View.GONE);
            itemHolder.controlName.setText(mContext.getString(R.string.balance));
            itemHolder.controlSeekbar.setMax(1000);
            itemHolder.controlSeekbar.setProgress((int)(500*Float.parseFloat(balanceString)));
            itemHolder.controlSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    balance = progress/500f;
                    if(balance <= 1.05f && balance >= 0.95f) {
                        balance = 1.0f;
                        itemHolder.controlSeekbar.setProgress((int) (500 * balance));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    sendMessage("setBalance",balance+"");
                }
            });

        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
