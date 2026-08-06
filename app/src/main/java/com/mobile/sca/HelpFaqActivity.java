package com.mobile.sca;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HelpFaqActivity extends AppCompatActivity {

    private static final int TOOLBAR_HEIGHT_DP = 56;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_faq);

        Toolbar toolbar =
                findViewById(R.id.helpFaqToolbar);

        applyStatusBarSpacing(toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {

            // Hide the default ActionBar title.
            // The centered TextView in XML displays the title.
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
                view -> getOnBackPressedDispatcher()
                        .onBackPressed()
        );
    }

    private void applyStatusBarSpacing(
            Toolbar toolbar
    ) {

        ViewCompat.setOnApplyWindowInsetsListener(
                toolbar,
                (view, insets) -> {

                    Insets statusBarInsets =
                            insets.getInsets(
                                    WindowInsetsCompat.Type
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
                            dpToPx(TOOLBAR_HEIGHT_DP)
                                    + statusBarInsets.top;

                    view.setLayoutParams(layoutParams);

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(toolbar);
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}