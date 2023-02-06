package ca.freda.relation_annotator.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;

public class OverviewFragment extends Fragment implements View.OnClickListener {

    protected ViewGroup rootView;
    private MainActivity activity;
    private JSONArray datasets;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.overview_slide_page, container, false);
        fillRootView();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        this.datasets = null;
    }

    protected void fillRootView() {
        rootView.findViewById(R.id.dataset_get_button).setOnClickListener(this);
        rootView.findViewById(R.id.button_logout).setOnClickListener(this);
        activity = (MainActivity) getActivity();
        System.out.println("fill root view");
    }

    public void setEmail(String email) {
        TextView displayNameTextView = rootView.findViewById(R.id.display_name_textview);
        displayNameTextView.setText(email);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dataset_get_button:
                ScrollView scrollView = rootView.findViewById(R.id.dataset_scrollview);
                scrollView.removeAllViews();
                try {
                    JSONObject message = new JSONObject();
                    message.put("mode", 5);
                    activity.comHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_logout:
                activity.logout();
                activity.setPagerItem(0);
                break;
            default:
                break;
        }
    }

    public void showDatasets(JSONArray datasets)throws JSONException {
        if (datasets == null) {
            datasets = this.datasets;
        } else {
            this.datasets = datasets;
        }
        System.out.println(datasets);
        TableLayout tableLayout = new TableLayout(activity);
        tableLayout.setOrientation(LinearLayout.VERTICAL);
        System.out.println("table layout: " + tableLayout);
        tableLayout.setShrinkAllColumns(true);
        tableLayout.setStretchAllColumns(true);

        TableRow tableRow = new TableRow(activity);
        tableRow.setBackgroundColor(Color.LTGRAY);
        TextView textView1 = new TextView(activity);
        textView1.setText("Relation");
        textView1.setTextSize(22);

        TextView textView8 = new TextView(activity);
        textView8.setText("Source");
        textView8.setTextSize(22);

        TextView textView2 = new TextView(activity);
        textView2.setText("Annot. 1");
        textView2.setTextSize(22);

        TextView textView3 = new TextView(activity);
        textView3.setText("Annot. 2");
        textView3.setTextSize(22);

        TextView textView4 = new TextView(activity);
        textView4.setText("Annot. 3");
        textView4.setTextSize(22);

        TextView textView5 = new TextView(activity);
        textView5.setText("Once");
        textView5.setTextSize(22);

        TextView textView6 = new TextView(activity);
        textView6.setText("Twice");
        textView6.setTextSize(22);

        TextView textView7 = new TextView(activity);
        textView7.setText("Full");
        textView7.setTextSize(22);

        tableRow.addView(textView1);
        tableRow.addView(textView8);
        tableRow.addView(textView2);
        tableRow.addView(textView3);
        tableRow.addView(textView4);
        tableRow.addView(textView5);
        tableRow.addView(textView6);
        tableRow.addView(textView7);
        tableLayout.addView(tableRow);

        for (int i = 0; i < datasets.length(); i++) {
            final JSONObject dataset = datasets.getJSONObject(i);
            final String datasetName = dataset.getString("name");
            String datasetSource = dataset.getString("dataset");

            int annotations_1 = dataset.getInt("annotations_1");
            int annotations_2 = dataset.getInt("annotations_2");
            int annotations_3 = dataset.getInt("annotations_3");
            int annotations_once = dataset.getInt("once");
            int annotations_twice = dataset.getInt("twice");
            int annotations_full = dataset.getInt("full");
            System.out.println(dataset);

            Button nameButton = new Button(activity);
            nameButton.setText(datasetName);
            nameButton.setTag(datasetName);
            nameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.comHandler.setDataset(dataset);
                    activity.setPagerItem(2);
                }
            });

            TextView textViewRows7 = new TextView(activity);
            textViewRows7.setText(datasetSource);

            TextView textViewRows1 = new TextView(activity);
            textViewRows1.setText(annotations_1 + "");

            TextView textViewRows2 = new TextView(activity);
            textViewRows2.setText(annotations_2 + "");

            TextView textViewRows3 = new TextView(activity);
            textViewRows3.setText(annotations_3 + "");

            TextView textViewRows4 = new TextView(activity);
            textViewRows4.setText(annotations_once + "");

            TextView textViewRows5 = new TextView(activity);
            textViewRows5.setText(annotations_twice + "");

            TextView textViewRows6 = new TextView(activity);
            textViewRows6.setText(annotations_full + "");

            tableRow = new TableRow(activity);
            tableRow.addView(nameButton);
            tableRow.addView(textViewRows7);
            tableRow.addView(textViewRows1);
            tableRow.addView(textViewRows2);
            tableRow.addView(textViewRows3);
            tableRow.addView(textViewRows4);
            tableRow.addView(textViewRows5);
            tableRow.addView(textViewRows6);
            tableLayout.addView(tableRow);
        }

        ScrollView scrollView = rootView.findViewById(R.id.dataset_scrollview);
        scrollView.removeAllViews();
        scrollView.addView(tableLayout);
    }
}
