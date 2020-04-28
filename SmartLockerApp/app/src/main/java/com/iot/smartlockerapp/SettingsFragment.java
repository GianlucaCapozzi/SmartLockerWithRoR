package com.iot.smartlockerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;

import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private String TAG = "SETTINGS";

    private static final String PREFS_NAME = "SmartLockSettings";

    private String email;
    private String name;

    private TextView usernameTV;
    private TextView emailTV;
    private TextView passTV;
    private TextView profileTV;
    private TextView deleteTV;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        SharedPreferences pref = this.getActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        name = pref.getString("user", null);
        email = pref.getString("email", null);

        final AccessToken accessToken = AccessToken.getCurrentAccessToken();

        Log.d(TAG, name + " " + email);

        usernameTV = v.findViewById(R.id.usernameSettings);
        String styledUsnameText = "<strong> Username Settings </strong>"
                + "<br />" + name;
        usernameTV.setText(Html.fromHtml(styledUsnameText, Html.FROM_HTML_MODE_LEGACY));

        usernameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                Intent i = new Intent(w.getContext(), ChangeUsernameActivity.class);
                startActivity(i);
            }
        });

        emailTV = v.findViewById(R.id.emailSettings);
        String styledEmailText = "<strong> Email Settings </strong>"
                + "<br />" + email;
        emailTV.setText(Html.fromHtml(styledEmailText, Html.FROM_HTML_MODE_LEGACY));

        emailTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                if(accessToken != null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("SETTINGS INFORMATION");
                    alertDialog.setMessage("Not possibile to change email because of Facebook login!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    Intent i = new Intent(w.getContext(), ChangeEmailActivity.class);
                    startActivity(i);
                }
            }
        });

        passTV = v.findViewById(R.id.passwordSettings);
        String styledPassText = "<strong> Password Settings </strong>"
                + "<br />Change your password";
        passTV.setText(Html.fromHtml(styledPassText, Html.FROM_HTML_MODE_LEGACY));

        passTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                if(accessToken != null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.MyAlertDialog)).create();
                    alertDialog.setTitle("SETTINGS INFORMATION");
                    alertDialog.setMessage("Not possible to change password because of Facebook login!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    Intent i = new Intent(w.getContext(), ChangePasswordActivity.class);
                    startActivity(i);
                }
            }
        });

        profileTV = v.findViewById(R.id.personalSettings);
        String styledPersText = "<strong> Profile Settings </strong>"
                + "<br />Profile picture, age, weight";
        profileTV.setText(Html.fromHtml(styledPersText, Html.FROM_HTML_MODE_LEGACY));

        profileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {
                //Toast.makeText(w.getContext(), "Clicked on personal settings", Toast.LENGTH_LONG).show();
                Intent i = new Intent(w.getContext(), ChangeProfileActivity.class);
                startActivity(i);
            }
        });

        deleteTV = v.findViewById(R.id.deleteAccount);
        String styledDeleteText = "<strong> Delete Account </strong>"
                + "<br />Delete permanently your account";
        deleteTV.setText(Html.fromHtml(styledDeleteText, Html.FROM_HTML_MODE_LEGACY));

        deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(), "Clicked on delete account", Toast.LENGTH_LONG).show();
                Intent i = new Intent(v.getContext(), DeleteActivity.class);
                startActivity(i);
            }
        });

        return v;

    }
}
