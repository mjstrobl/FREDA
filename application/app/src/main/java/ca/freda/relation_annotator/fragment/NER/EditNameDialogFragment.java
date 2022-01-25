package ca.freda.relation_annotator.fragment.NER;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

import ca.freda.relation_annotator.R;
import ca.freda.relation_annotator.data.Entity;
import ca.freda.relation_annotator.listener.TypeButtonDragEventListener;

public class EditNameDialogFragment extends DialogFragment {

    private EditText mEditText;

    public EditNameDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static EditNameDialogFragment newInstance(ArrayList<String> types, int index) {
        EditNameDialogFragment frag = new EditNameDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("subtypes",types);
        args.putInt("index",index);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_name, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<String> types = getArguments().getStringArrayList("subtypes");
        int index = getArguments().getInt("index");


        LinearLayout layout = new LinearLayout(getActivity());

        ScrollView scrollView = view.findViewById(R.id.subtype_scrollview);
        scrollView.removeAllViews();

        layout.setId(R.id.subtype_scrollview_layout);
        layout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            Button button = new Button(getActivity());
            button.setActivated(false);
            button.setTypeface(null, Typeface.NORMAL);
            button.setTag(type);
            button.setText(type);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String type = (String)v.getTag();
                    NERAnnotationFragment nerAnnotationFragment = (NERAnnotationFragment) getTargetFragment();
                    nerAnnotationFragment.getData().getEntity(index).setType(type);
                    nerAnnotationFragment.fillEntityButtonScrollView();
                    getDialog().dismiss();
                }
            });

            layout.addView(button);
        }
        scrollView.addView(layout);
    }
}
