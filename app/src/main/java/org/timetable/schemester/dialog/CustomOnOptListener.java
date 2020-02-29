package org.timetable.schemester.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import org.timetable.schemester.listener.OnOptionChosenListener;
import org.timetable.schemester.R;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;


public class CustomOnOptListener extends AppCompatDialog {
    private OnOptionChosenListener onOptionChosenListener;
    private  Button set;
    public CustomOnOptListener(Context context, OnOptionChosenListener onOptionChosenListener) {
        super(context);
        this.onOptionChosenListener = onOptionChosenListener;
    }
    private ImageView choiceimg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_radio_choice_dialog);
        Objects.requireNonNull(this.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final TextView choiceHead;
        RadioGroup radioGroup;
        set = findViewById(R.id.choice_submit);
        Button cancel = findViewById(R.id.choice_cancel);
        choiceimg = findViewById(R.id.choice_dialog_image);
        radioGroup = findViewById(R.id.radiogroup);
        RadioButton r102 = findViewById(R.id.radio2);
        RadioButton r101 = findViewById(R.id.radio1);
        assert r101!=null;
        assert r102!=null;
        if(getThemeStatus()==101) {
            r101.setChecked(true);
            choiceimg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_schemesterlightmockup));
        }
        else if(getThemeStatus()==102){
            r102.setChecked(true);
            choiceimg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_schemesterdarkmockup));
        }
        else{
            r101.setChecked(true);
            choiceimg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_schemesterlightmockup));
        }

        Objects.requireNonNull(cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        
        if (radioGroup != null) {
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup rGroup, final int radioid) {
                    switch (radioid) {
                        case R.id.radio1:
                            choiceimg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_schemesterlightmockup));
                            break;
                        case R.id.radio2:
                            choiceimg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_schemesterdarkmockup));
                            break;
                            default:choiceimg.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_schemesterlightmockup));
                    }
                    set.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switch (radioid) {
                                case R.id.radio1:
                                    storeThemeStatus(101);
                                    onOptionChosenListener.onChoice(101);
                                    break;
                                case R.id.radio2:
                                    storeThemeStatus(102);
                                    onOptionChosenListener.onChoice(102);
                                    break;
                            }
                            dismiss();
                        }
                    });
                }
            });
        }
    }
    private void storeThemeStatus(int themechoice){
        SharedPreferences mSharedPreferences = getContext().getSharedPreferences("schemeTheme", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("themeCode", themechoice);
        mEditor.apply();
    }

    private int getThemeStatus() {
        SharedPreferences mSharedPreferences = this.getContext().getSharedPreferences("schemeTheme", MODE_PRIVATE);
        return mSharedPreferences.getInt("themeCode", 0);
    }
}
