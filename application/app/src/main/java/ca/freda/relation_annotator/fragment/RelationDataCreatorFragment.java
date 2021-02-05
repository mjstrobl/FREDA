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

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RelationDataCreatorFragment extends Fragment implements View.OnClickListener {

    private ViewGroup rootView;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.relation_slide_page, container, false);
        
        rootView.findViewById(R.id.relation_get_button).setOnClickListener(this);

        activity = (MainActivity) getActivity();

        TextView uidTextView = rootView.findViewById(R.id.relation_uid_textview);
        uidTextView.setText(activity.getUID());

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.relation_get_button:
                ScrollView scrollView = rootView.findViewById(R.id.relations_scrollview);
                scrollView.removeAllViews();
                activity.sendMessageWithMode(5);
            default:
                break;
        }
    }

    public void showRelations(JSONArray relations)throws JSONException {
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

        TextView textView2 = new TextView(activity);
        textView2.setText("Annotations 1");
        textView2.setTextSize(22);

        TextView textView3 = new TextView(activity);
        textView3.setText("Annotations 2");
        textView3.setTextSize(22);

        TextView textView4 = new TextView(activity);
        textView4.setText("Annotations 3");
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
        tableRow.addView(textView2);
        tableRow.addView(textView3);
        tableRow.addView(textView4);
        tableRow.addView(textView5);
        tableRow.addView(textView6);
        tableRow.addView(textView7);
        tableLayout.addView(tableRow);



        for (int i = 0; i < relations.length(); i++) {
            final JSONObject relation = relations.getJSONObject(i);
            final String relationName = relation.getString("name");
            int annotations_1 = relation.getInt("annotations_1");
            int annotations_2 = relation.getInt("annotations_2");
            int annotations_3 = relation.getInt("annotations_3");
            int annotations_once = relation.getInt("once");
            int annotations_twice = relation.getInt("twice");
            int annotations_full = relation.getInt("full");
            System.out.println(relation);
            //JSONArray keywords = relation.getJSONArray("keywords");
            JSONObject response = relation.getJSONObject("response");


            Button nameButton = new Button(activity);
            nameButton.setText(relationName);
            nameButton.setTag(relationName);
            nameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.setRelation(relation);
                    activity.setPagerItem(1);
                }
            });

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
            tableRow.addView(textViewRows1);
            tableRow.addView(textViewRows2);
            tableRow.addView(textViewRows3);
            tableRow.addView(textViewRows4);
            tableRow.addView(textViewRows5);
            tableRow.addView(textViewRows6);
            tableLayout.addView(tableRow);
        }

        ScrollView scrollView = rootView.findViewById(R.id.relations_scrollview);
        scrollView.removeAllViews();
        scrollView.addView(tableLayout);
    }
}
