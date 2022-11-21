package ca.freda.relation_annotator.fragment.EL;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Iterator;

import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.data.EntityButtonProperty;
import ca.freda.relation_annotator.fragment.NER.EditNameDialogFragment;
import ca.freda.relation_annotator.fragment.NER.NERAnnotationFragment;

public class CandidateSelectionDialog extends DialogFragment {

    public CandidateSelectionDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CandidateSelectionDialog newInstance(JSONArray candidates, int index) {
        CandidateSelectionDialog frag = new CandidateSelectionDialog();
        Bundle args = new Bundle();
        args.putString("candidates",candidates.toString());
        args.putInt("index",index);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.entity_linking_page, container);
    }
    private Point getDisplaySize() {
        Window window = getDialog().getWindow();
        Point displaySize = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(displaySize);

        return displaySize;
    }

    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        Point displaySize = getDisplaySize();

        int width = displaySize.x;
        int height = displaySize.y;

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int)(width * 0.9);
        params.height = (int)(height * 0.9);

        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Point displaySize = getDisplaySize();

        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        try {
            JSONArray candidates = new JSONArray(getArguments().getString("candidates"));

            int index = getArguments().getInt("index");


            LinearLayout layout = new LinearLayout(getActivity());

            ScrollView scrollView = view.findViewById(R.id.entity_linking_scrollview);
            scrollView.removeAllViews();

            layout.setOrientation(LinearLayout.VERTICAL);

            TableLayout tableLayout = new TableLayout(getActivity());
            tableLayout.setOrientation(LinearLayout.VERTICAL);
            System.out.println("table layout: " + tableLayout);
            //tableLayout.setShrinkAllColumns(true);
            //tableLayout.setStretchAllColumns(true);

            TableRow tableRow = new TableRow(getActivity());
            tableRow.setBackgroundColor(Color.LTGRAY);
            TextView textView1 = new TextView(getActivity());
            textView1.setText("Entity");
            textView1.setTextSize(22);

            TextView textView2 = new TextView(getActivity());
            textView2.setText("Abstract");
            textView2.setTextSize(22);


            tableRow.addView(textView1);
            tableRow.addView(textView2);
            tableLayout.addView(tableRow);
            for (int i = 0; i < candidates.length(); i++) {
                String candidate = ((JSONArray)candidates.get(i)).getString(0);
                String candidtateAbstract = ((JSONArray)candidates.get(i)).getString(1);

                Button button = new Button(getActivity());
                button.setActivated(false);
                button.setTypeface(null, Typeface.NORMAL);
                button.setTag(candidate);
                button.setText(candidate);
                button.setWidth((int)Math.round(displaySize.x*0.25*0.9));
                button.setTypeface(null, Typeface.BOLD);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ELAnnotationFragment elAnnotationFragment = (ELAnnotationFragment) getTargetFragment();
                        if (candidate.equals("NO ENTITY")) {
                            elAnnotationFragment.getData().getEntity(index).setWikiName(null);
                        } else {
                            elAnnotationFragment.getData().getEntity(index).setWikiName(candidate);

                        }
                        elAnnotationFragment.fillEntityButtonScrollView();
                        elAnnotationFragment.fillTextView(false);
                        getDialog().dismiss();
                    }
                });

                TextView textViewRows = new TextView(getActivity());
                textViewRows.setText(candidtateAbstract);
                textViewRows.setWidth((int)Math.round(displaySize.x*0.65*0.9));
                tableRow = new TableRow(getActivity());
                tableRow.addView(button);
                tableRow.addView(textViewRows);
                tableLayout.addView(tableRow);
            }
            scrollView.addView(tableLayout);

        } catch (JSONException e) {
            e.printStackTrace();
            getDialog().dismiss();
        }
    }
}
