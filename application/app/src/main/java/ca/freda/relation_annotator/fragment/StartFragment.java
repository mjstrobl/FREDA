package ca.freda.relation_annotator.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;

public class StartFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ViewGroup rootView;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.start_slide_page, container, false);

        rootView.findViewById(R.id.re).setOnClickListener(this);
        rootView.findViewById(R.id.el).setOnClickListener(this);
        rootView.findViewById(R.id.cr).setOnClickListener(this);
        rootView.findViewById(R.id.ner).setOnClickListener(this);

        rootView.findViewById(R.id.online_switch).setOnClickListener(this);
        Switch onlineSwitch = (Switch) rootView.findViewById(R.id.online_switch);
        onlineSwitch.setOnCheckedChangeListener(this);

        activity = (MainActivity) getActivity();

        return rootView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.online_switch:
                activity.comHandler.setOnline(buttonView.isChecked());
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.re:
                activity.setPagerItem(4);
                break;
            case R.id.el:
                activity.setPagerItem(3);
                break;
            case R.id.cr:
                activity.setPagerItem(2);
                break;
            case R.id.ner:
                activity.setPagerItem(1);
                break;
            default:
                activity.setPagerItem(0);
                break;
        }
    }
}
