package com.mobile.sca;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HelpFaqActivity extends AppCompatActivity {

    private static final int TOOLBAR_HEIGHT_DP = 56;

    // Change these two values with your real support details
    private static final String SUPPORT_EMAIL =
            "support@yourdomain.com";

    private static final String SUPPORT_PHONE =
            "+919876543210";

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_help_faq
        );

        Toolbar toolbar =
                findViewById(
                        R.id.helpFaqToolbar
                );

        applyStatusBarSpacing(toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            // Hide default ActionBar title
            getSupportActionBar()
                    .setDisplayShowTitleEnabled(false);

            getSupportActionBar()
                    .setDisplayShowHomeEnabled(false);

            getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(false);
        }

        toolbar.setNavigationIcon(
                R.drawable.ic_arrow_back_material
        );

        toolbar.setNavigationContentDescription(
                R.string.back_to_previous_screen
        );

        toolbar.setNavigationOnClickListener(
                view ->
                        getOnBackPressedDispatcher()
                                .onBackPressed()
        );

        setupContactSupport();
    }

    private void setupContactSupport() {

        TextView supportEmail =
                findViewById(
                        R.id.txtSupportEmail
                );

        TextView supportPhone =
                findViewById(
                        R.id.txtSupportPhone
                );

        supportEmail.setText(
                "Email: " + SUPPORT_EMAIL
        );

        supportPhone.setText(
                "Phone: " + SUPPORT_PHONE
        );

        supportEmail.setOnClickListener(
                view -> openEmail()
        );

        supportPhone.setOnClickListener(
                view -> openPhoneDialer()
        );
    }

    private void openEmail() {

        Intent emailIntent =
                new Intent(
                        Intent.ACTION_SENDTO
                );

        emailIntent.setData(
                Uri.parse(
                        "mailto:" + SUPPORT_EMAIL
                )
        );

        emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "Smart Call Assistant Support"
        );

        try {

            startActivity(emailIntent);

        } catch (Exception exception) {

            Toast.makeText(
                    this,
                    "No email application found",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openPhoneDialer() {

        Intent phoneIntent =
                new Intent(
                        Intent.ACTION_DIAL
                );

        phoneIntent.setData(
                Uri.parse(
                        "tel:" + SUPPORT_PHONE
                )
        );

        try {

            startActivity(phoneIntent);

        } catch (Exception exception) {

            Toast.makeText(
                    this,
                    "Unable to open phone dialer",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void applyStatusBarSpacing(
            Toolbar toolbar
    ) {

        ViewCompat.setOnApplyWindowInsetsListener(
                toolbar,
                (view, insets) -> {

                    Insets statusBarInsets =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .statusBars()
                            );

                    view.setPadding(
                            view.getPaddingLeft(),
                            statusBarInsets.top,
                            view.getPaddingRight(),
                            view.getPaddingBottom()
                    );

                    ViewGroup.LayoutParams layoutParams =
                            view.getLayoutParams();

                    layoutParams.height =
                            dpToPx(
                                    TOOLBAR_HEIGHT_DP
                            )
                                    + statusBarInsets.top;

                    view.setLayoutParams(
                            layoutParams
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(
                toolbar
        );
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}