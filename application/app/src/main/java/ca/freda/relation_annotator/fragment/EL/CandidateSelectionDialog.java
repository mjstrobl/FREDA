package ca.freda.relation_annotator.fragment.EL;

import android.content.Context;
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

    private EditText mEditText;

    public CandidateSelectionDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CandidateSelectionDialog newInstance(JSONObject candidates, int index) {
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
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = width - 30;
        params.height = height - 30;


        System.out.println("width: " + width);
        System.out.println("height: " + height);

        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        return inflater.inflate(R.layout.fragment_edit_name, container);
    }

    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int)(width * 0.9);
        params.height = (int)(height * 0.9);

        //window.setLayout((int) (width * 0.90),(int) (height * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            JSONObject candidates = new JSONObject(getArguments().getString("candidates"));

            int index = getArguments().getInt("index");


            LinearLayout layout = new LinearLayout(getActivity());

            ScrollView scrollView = view.findViewById(R.id.subtype_scrollview);
            scrollView.removeAllViews();

            layout.setOrientation(LinearLayout.VERTICAL);

            Iterator<String> keys = candidates.keys();

            TableLayout tableLayout = new TableLayout(getActivity());
            tableLayout.setOrientation(LinearLayout.VERTICAL);
            System.out.println("table layout: " + tableLayout);
            tableLayout.setShrinkAllColumns(true);
            tableLayout.setStretchAllColumns(true);

            while(keys.hasNext()) {
                String candidate = keys.next();
                String candidtateAbstract = candidates.getString(candidate);




                Button button = new Button(getActivity());
                button.setActivated(false);
                button.setTypeface(null, Typeface.NORMAL);
                button.setTag(candidate);
                button.setText(candidate);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String type = (String)v.getTag();
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
                TableRow tableRow = new TableRow(getActivity());
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
