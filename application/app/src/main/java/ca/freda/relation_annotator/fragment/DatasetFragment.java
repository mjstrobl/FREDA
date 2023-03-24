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

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;

public class DatasetFragment extends Fragment implements View.OnClickListener {

    protected ViewGroup rootView;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.dataset_slide_page, container, false);
        fillRootView();
        return rootView;
    }

    protected void fillRootView() {
        rootView.findViewById(R.id.button_back).setOnClickListener(this);
        activity = (MainActivity) getActivity();
        System.out.println("fill root view");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                activity.setPagerItem(1);
                break;
            default:
                break;
        }
    }

    public void showRelations(JSONArray relations, String dataset, boolean active) throws JSONException {
        String extended_dataset_name = active ? dataset + " (active)" : dataset + " (not active)";
        ((TextView)rootView.findViewById(R.id.dataset_name_textview)).setText(extended_dataset_name);
        System.out.println(relations);
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
        textView8.setText("Sentences");
        textView8.setTextSize(22);

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
        tableRow.addView(textView5);
        tableRow.addView(textView6);
        tableRow.addView(textView7);
        tableLayout.addView(tableRow);

        for (int i = 0; i < relations.length(); i++) {
            final JSONArray relation = relations.getJSONArray(i);
            final String relationName = relation.getString(0);
            final boolean relationActive = relation.getBoolean(1);
            final String info = relation.getString(2);
            final int  sentences = relation.getInt(3);
            final int once = relation.getInt(4);
            final int twice = relation.getInt(5);
            final int full = relation.getInt(6);

            Button nameButton = new Button(activity);
            nameButton.setText(relationName);
            nameButton.setTag(relationName);
            nameButton.setEnabled(relationActive);
            nameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.getPagerAdapter().getAnnotationFragment().setDatasetInfo(dataset, relationName, info, 1);
                    activity.getPagerAdapter().getAnnotationFragment().getSentence();
                    activity.setPagerItem(3);
                }
            });

            TextView textViewRows1 = new TextView(activity);
            textViewRows1.setText(sentences + "");

            TextView textViewRows2 = new TextView(activity);
            textViewRows2.setText(once + "");

            TextView textViewRows3 = new TextView(activity);
            textViewRows3.setText(twice + "");

            TextView textViewRows4 = new TextView(activity);
            textViewRows4.setText(full + "");

            tableRow = new TableRow(activity);
            tableRow.addView(nameButton);
            tableRow.addView(textViewRows1);
            tableRow.addView(textViewRows2);
            tableRow.addView(textViewRows3);
            tableRow.addView(textViewRows4);
            tableLayout.addView(tableRow);
        }

        ScrollView scrollView = rootView.findViewById(R.id.dataset_scrollview);
        scrollView.removeAllViews();
        scrollView.addView(tableLayout);
    }
}
