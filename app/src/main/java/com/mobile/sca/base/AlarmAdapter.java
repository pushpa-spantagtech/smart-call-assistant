package com.mobile.sca.base;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;

import static com.mobile.sca.base.TimeUtils.getDayOfWeek;
import static com.mobile.sca.base.TimeUtils.to24Hour;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.sca.AlarmDatabase;
import com.mobile.sca.AlarmEntity;
import com.mobile.sca.HomeFrag;
import com.mobile.sca.R;
import com.mobile.sca.ScheduleFrag;
import com.mobile.sca.home;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.ViewHolder> {

    private Context context;
    private List<AlarmModal> list;

    RelativeLayout durlayout;

    SharedPreferences pref ;

    public AlarmAdapter(Context context, List<AlarmModal> list) {
        this.context = context;
        this.list = list;
        pref = context.getSharedPreferences("status",MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_alarm, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AlarmModal model = list.get(position);
        Log.e("Test....", ""+model.entity.title);

        holder.duration.setText(String.format("%d minutes", model.duration));
        if ((model.entity.day == model.entity.endday && model.entity.month == model.entity.endmonth
                && model.entity.year == model.entity.endyear)){
            if(model.entity.endday != 0) {
                String date = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.day,
                        model.entity.month + 1,
                        model.entity.year
                );
                String enddate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.endday,
                        model.entity.endmonth + 1,
                        model.entity.endyear
                );
                holder.dateRange.setText(date +" - "+enddate);
            } else {
                holder.dateRange.setText("Single Occurrence");
            }
            holder.title.setText(model.entity.title);
        }
        else {
            if(model.entity.endday != 0) {
                String date = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.day,
                        model.entity.month + 1,
                        model.entity.year
                );
                String enddate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.endday,
                        model.entity.endmonth + 1,
                        model.entity.endyear
                );
                holder.title.setText(model.entity.title);
                holder.dateRange.setText(date +" - "+enddate);
            }
            else {
                holder.title.setText(model.entity.title);
                holder.dateRange.setText("Single Occurrence");
            }
        }
        String[] parts = holder.title.getText().toString().split("::");
        holder.title.setText(parts[0]);
        if(model.entity.endday == 0) {
            holder.status.setText(parts[1]);
            if(parts[1].contains("ACTIVE")) {
                holder.status.setBackground(context.getDrawable(R.drawable.bg_active));
                holder.switchonoff.setChecked(true);
            } else if(parts[1].contains("CANCELLED")) {
                holder.status.setBackground(context.getDrawable(R.drawable.bg_cancelled));
                holder.switchonoff.setChecked(false);
            } else if(parts[1].contains("Completed")) {
                holder.status.setBackground(context.getDrawable(R.drawable.bg_completed));
                holder.switchonoff.setChecked(false);
            } else {
                holder.status.setBackground(context.getDrawable(R.drawable.bg_status_fired));
                holder.switchonoff.setChecked(true);
            }
        }
        else {
            // if(model.entity.endday != 0)
//            holder.switchonoff.setVisibility(GONE);
//            holder.delete.setVisibility(GONE);
//            holder.status.setVisibility(GONE);

            SharedPreferences prefs =
                    context.getSharedPreferences(""+model.entity.id, Context.MODE_PRIVATE);

            Set<String> savedSet =
                    prefs.getStringSet("KEY_REQUEST_CODES", new HashSet<>());

            int finished = 0;
            for (String item : savedSet) {
                Log.e("AdapterItem", item);
                if(item.contains("ACTIVE")) {
                    holder.status.setText("ACTIVE");
                    holder.switchonoff.setChecked(true);
                    holder.status.setBackground(context.getDrawable(R.drawable.bg_active));
                    break;
                } else if(item.contains("CANCELLED")) {
                    holder.status.setText("CANCELLED");
                    holder.switchonoff.setChecked(false);
                    holder.status.setBackground(context.getDrawable(R.drawable.bg_cancelled));
                    break;
                } else if(item.contains("Completed")) {
                    finished ++;
                } else {
                    holder.status.setText("FIRED");
                    holder.switchonoff.setChecked(true);
                    holder.status.setBackground(context.getDrawable(R.drawable.bg_active));
                    break;
                }
            }
            if(savedSet.size() == finished) {
                holder.status.setText("Completed");
                holder.status.setBackground(context.getDrawable(R.drawable.bg_completed));
                holder.switchonoff.setChecked(false);
            }
            Log.e("Count:;", ""+savedSet.size() +" " +finished);
        }

        holder.txtTime.setText(model.time);
        if(model.days.isEmpty()) {
            if(model.entity.endday == 0) {
                String date = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        model.entity.day,
                        model.entity.month + 1,
                        model.entity.year
                );
                holder.txtDesc.setText(date.replaceAll(",",", "));
            } else {
                holder.txtDesc.setText("Mon, Tue, Wed, Thu, Fri, Sat, Sun");
            }
        } else {
            holder.txtDesc.setText(model.days.replaceAll(",",", "));
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = getBuilder(model);
                builder1.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        holder.switchonoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                if(!b) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Are you sure you want to turn off this schedule?");
                    builder.setTitle("Turn Off");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes, Turn Off", (DialogInterface.OnClickListener) (dialog, which) -> {Gson gson = new Gson();
                        if(model.entity.endday != 0) {
                            TimeUtils.cancelWeeklyAlarmsWithDate(context, model.entity);
                            holder.switchonoff.setChecked((false));
                            //context.deleteSharedPreferences(String.valueOf(model.entity.id));

                        } else {
                            cancelOneTimeAlarm(context, model.entity);
                            holder.switchonoff.setChecked((false));
                        }
                        HomeFrag.ins.reload();
                    });
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                        dialog.cancel();
                        holder.switchonoff.setChecked((true));
                    });

                    // Create the Alert dialog
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                } else {
                    if(holder.status.getText().equals("OFF") || holder.status.getText().equals("FIRED") || holder.status.getText().equals("CANCELLED") || holder.status.getText().equals("Completed")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Sorry, you could not enable this schedule directly, instead of turn on, you can edit and update the schedule details.");
                        builder.setTitle("Info!");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Create New", (DialogInterface.OnClickListener) (dialog, which) -> {Gson gson = new Gson();
                        String alarmJson = gson.toJson(model.entity);

                        SharedPreferences pref = context.getSharedPreferences("app", context.MODE_PRIVATE);
                        pref.edit().putString("alarm", alarmJson).commit();
                        home.instance.moveTab(2);
                        });
                        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                            dialog.cancel();
                        });

                        // Create the Alert dialog
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        holder.switchonoff.setChecked((false));

                    }

//                    else {
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                        builder.setMessage("Sorry, you could not enable this schedule directly, instead of turn on, you can edit and update the schedule details.");
//                        builder.setTitle("Info!");
//                        builder.setCancelable(false);
//                        builder.setPositiveButton("Create New", (DialogInterface.OnClickListener) (dialog, which) -> {Gson gson = new Gson();
//                            String alarmJson = gson.toJson(model.entity);
//
//                            SharedPreferences pref = context.getSharedPreferences("app", context.MODE_PRIVATE);
//                            pref.edit().putString("alarm", alarmJson).commit();
//                            home.instance.moveTab(2);
//                        });
//                        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
//                            dialog.cancel();
//                        });
//
//                        // Create the Alert dialog
//                        AlertDialog alertDialog = builder.create();
//                        alertDialog.show();
//                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTime, status, txtDesc, duration, title, dateRange;
        SwitchCompat switchonoff;
        RelativeLayout daysc;

        MaterialCardView card;

        Button delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtTime);
            card = itemView.findViewById(R.id.card);
            txtDesc = itemView.findViewById(R.id.days);
            status = itemView.findViewById(R.id.status);
            dateRange = itemView.findViewById(R.id.dateRange);
            switchonoff = itemView.findViewById(R.id.switchonoff);
            duration = itemView.findViewById(R.id.durationTxt);
            title = itemView.findViewById(R.id.title);
            delete = itemView.findViewById(R.id.delete);
        }
    }
    private float dpToPx(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public void cancelOneTimeAlarm(Context context, AlarmEntity alarm) {

        Intent intent = new Intent(context, AlarmReceiver.class);
//        intent.putExtra("ALARM_ID", alarm.id);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarm.id, // SAME requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pi);
        pi.cancel();

        AlarmDatabase.getInstance(context)
                .alarmDao()
                .updateAlarmTitle(alarm.id, alarm.title.replace("ACTIVE","CANCELLED"));

        pi = PendingIntent.getBroadcast(
                context,
                (alarm.id + 999), // SAME requestCode
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Log.e("EqualCondition" , "..>>."+ pref.getInt("alarmId",0) +" == "+alarm.id);
//        if(pref.getInt("alarmId",0) == alarm.id) {
            alarm.id = (alarm.id + 999);
            am.cancel(pi);
            //DndUtils.setDnd(context, false);
        //}

        Log.e("AlarmCancel", "One-time alarm cancelled: " + alarm.title);
    }
     AlertDialog.Builder getBuilder(AlarmModal model) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Are you sure you want to delete it?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //put your code that needed to be executed when okay is clicked
                        HomeFrag.deleteNow(model.entity);
                        if(model.entity.endday != 0) {
                            TimeUtils.cancelWeeklyAlarmsWithDate(context, model.entity);
                            //context.deleteSharedPreferences(String.valueOf(model.entity.id));
                        }
                        else {
                            cancelOneTimeAlarm(context, model.entity);
                        }
                        dialog.cancel();
                    }
                });
        return builder1;
    }

}
