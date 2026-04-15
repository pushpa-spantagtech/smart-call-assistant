package com.mobile.sca;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SupportUsFrag extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.support_us, container, false);
        TextView tvAboutUs = root.findViewById(R.id.tvAboutUs);
        TextView tvTerms = root.findViewById(R.id.tvTerms);
        TextView tvPrivacy = root.findViewById(R.id.tvPrivacy);
        TextView tvContactUs = root.findViewById(R.id.tvContactUs);

        return root;
    }
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
