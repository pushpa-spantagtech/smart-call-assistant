package com.mobile.sca;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mobile.sca.base.login;

public class SupportUsFrag extends Fragment {

    private static final String PREF_NAME = "app";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_PHONE = "phone";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View root = inflater.inflate(
                R.layout.support_us,
                container,
                false
        );

        TextView tvHelpFaq = root.findViewById(R.id.tvHelpFaq);
        TextView tvLogout = root.findViewById(R.id.tvLogout);

        tvHelpFaq.setOnClickListener(view -> openHelpFaqPage());
        tvLogout.setOnClickListener(view -> showLogoutDialog());

        return root;
    }

    private void openHelpFaqPage() {

        if (!isAdded() || getActivity() == null) {
            return;
        }

        Intent intent = new Intent(
                requireActivity(),
                HelpFaqActivity.class
        );

        startActivity(intent);
    }

    private void showLogoutDialog() {

        if (!isAdded() ||
                getContext() == null ||
                getActivity() == null) {

            return;
        }

        new MaterialAlertDialogBuilder(
                requireContext(),
                R.style.AppMaterialDialogTheme
        )
                .setIcon(R.drawable.ic_error_outline)
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_message)
                .setCancelable(true)
                .setNegativeButton(
                        R.string.cancel,
                        (dialog, which) -> dialog.dismiss()
                )
                .setPositiveButton(
                        R.string.logout,
                        (dialog, which) -> {
                            dialog.dismiss();
                            performLogout();
                        }
                )
                .show();
    }

    private void performLogout() {

        if (!isAdded() ||
                getContext() == null ||
                getActivity() == null) {

            return;
        }

        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .remove(KEY_LOGGED_IN)
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_PHONE)
                .apply();

        Intent intent = new Intent(
                requireActivity(),
                login.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        requireActivity().finish();
    }
}
