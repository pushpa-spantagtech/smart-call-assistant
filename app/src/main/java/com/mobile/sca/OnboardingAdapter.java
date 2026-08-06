package com.mobile.sca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hbb20.CountryCodePicker;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private static final int TOTAL_PAGES = 3;
    private final OnboardingActionListener listener;
    Context context;

    public OnboardingAdapter(OnboardingActionListener listener, Context applicationContext) {
        this.listener = listener;
        context = applicationContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layoutId;
        switch (viewType) {
            case 0:
                layoutId = R.layout.item_phone;
                break;
            case 1:
                layoutId = R.layout.item_otp;
                break;
            default:
                layoutId = R.layout.item_name;
                break;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return TOTAL_PAGES;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CountryCodePicker ccp;
        EditText etPhone;
        Button continueBtn, verify, signup;

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if (viewType == 0) {
                ccp = itemView.findViewById(R.id.ccp);
                etPhone = itemView.findViewById(R.id.etPhone);
                continueBtn = itemView.findViewById(R.id.continuebutton);

                ccp.registerCarrierNumberEditText(etPhone);

                continueBtn.setOnClickListener(v -> {
                    if (ccp.isValidFullNumber()) {
                        String phone = ccp.getFullNumberWithPlus();
                        listener.onPhoneContinueClicked(phone, viewType + 1);
                    } else {
                        Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (viewType == 1) {
                verify = itemView.findViewById(R.id.verify);
                verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onPhoneContinueClicked("phone", viewType + 1);
                    }
                });
            } else if (viewType == 2) {
                signup = itemView.findViewById(R.id.signup);
                signup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onPhoneContinueClicked("phone", viewType + 1);
                    }
                });
            }
        }
    }
}
