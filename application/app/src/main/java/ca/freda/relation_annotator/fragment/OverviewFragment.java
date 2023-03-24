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

import com.amazonaws.http.HttpMethodName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import ca.freda.relation_annotator.MainActivity;
import ca.freda.relation_annotator.R;

public class OverviewFragment extends Fragment implements View.OnClickListener {

    protected ViewGroup rootView;
    private MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.overview_slide_page, container, false);
        fillRootView();
        return rootView;
    }

    protected void fillRootView() {
        rootView.findViewById(R.id.relations_get_button).setOnClickListener(this);
        rootView.findViewById(R.id.button_back).setOnClickListener(this);
        activity = (MainActivity) getActivity();
        System.out.println("fill root view");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.relations_get_button:
                activity.getGatewayHandler().doInvokeAPI(HttpMethodName.GET, "dataset", new HashMap<>(), null);
                break;
            case R.id.button_back:
                activity.setPagerItem(0);
                break;
            default:
                break;
        }
    }

    public void showDatasets(JSONObject datasets) throws JSONException {
        System.out.println(datasets);
        TableLayout tableLayout = new TableLayout(activity);
        tableLayout.setOrientation(LinearLayout.VERTICAL);
        System.out.println("table layout: " + tableLayout);
        tableLayout.setShrinkAllColumns(true);
        tableLayout.setStretchAllColumns(true);

        TableRow tableRow = new TableRow(activity);
        tableRow.setBackgroundColor(Color.LTGRAY);
        TextView textView1 = new TextView(activity);
        textView1.setText("Dataset");
        textView1.setTextSize(22);

        TextView textView8 = new TextView(activity);
        textView8.setText("Relations");
        textView8.setTextSize(22);

        TextView textView2 = new TextView(activity);
        textView2.setText("Active");
        textView2.setTextSize(22);

        TextView textView3 = new TextView(activity);
        textView3.setText("Annotations");
        textView3.setTextSize(22);

        TextView textView4 = new TextView(activity);
        textView4.setText("Sentences");
        textView4.setTextSize(22);


        tableRow.addView(textView1);
        tableRow.addView(textView8);
        tableRow.addView(textView2);
        tableRow.addView(textView3);
        tableRow.addView(textView4);
        tableLayout.addView(tableRow);

        Iterator<String> keys = datasets.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            if (datasets.get(key) instanceof JSONObject) {
                JSONArray relations = datasets.getJSONObject(key).getJSONArray("relations");
                boolean active = datasets.getJSONObject(key).getBoolean("active");
                int sentences = datasets.getJSONObject(key).getInt("sentences");
                int annotations = datasets.getJSONObject(key).getInt("annotations");
                String datasetName = key;

                Button nameButton = new Button(activity);
                nameButton.setText(datasetName);
                nameButton.setTag(datasetName);
                nameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            activity.getPagerAdapter().getDatasetFragment().showRelations(relations, datasetName, active);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        activity.setPagerItem(2);
                    }
                });

                TextView textViewRows1 = new TextView(activity);
                textViewRows1.setText(relations.length() + "");

                TextView textViewRows2 = new TextView(activity);
                textViewRows2.setText(active + "");

                TextView textViewRows3 = new TextView(activity);
                textViewRows3.setText(annotations + "");

                TextView textViewRows4 = new TextView(activity);
                textViewRows4.setText(sentences + "");

                tableRow = new TableRow(activity);
                tableRow.addView(nameButton);
                tableRow.addView(textViewRows1);
                tableRow.addView(textViewRows2);
                tableRow.addView(textViewRows3);
                tableRow.addView(textViewRows4);
                tableLayout.addView(tableRow);
            }
        }

        ScrollView scrollView = rootView.findViewById(R.id.dataset_scrollview);
        scrollView.removeAllViews();
        scrollView.addView(tableLayout);
    }
}
